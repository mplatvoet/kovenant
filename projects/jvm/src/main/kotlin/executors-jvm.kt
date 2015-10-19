/*
 * Copyright (c) 2015 Mark Platvoet<mplatvoet@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * THE SOFTWARE.
 */

package nl.komponents.kovenant.jvm

import nl.komponents.kovenant.Dispatcher
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference


/**
 * Convenience method to convert an Executor to an Dispatcher
 */
public fun Executor.asDispatcher(): Dispatcher = when (this) {
    is Dispatcher -> this
    is ExecutorService -> ExecutorServiceDispatcher(this)
    else -> ExecutorDispatcher(this)
}

public fun Dispatcher.asExecutor(): Executor = when (this) {
    is Executor -> this
    else -> DispatcherExecutor(this)
}

public fun Dispatcher.asExecutorService(): ExecutorService = when (this) {
    is ExecutorService -> this
    else -> DispatcherExecutorService(this)
}

private open class ExecutorDispatcher(private val executor: Executor) : Dispatcher, Executor by executor {
    override val terminated: Boolean get() {
        throw UnsupportedOperationException("Don't know how to determine if $executor is terminated")
    }

    override val stopped: Boolean get() {
        throw UnsupportedOperationException("Don't know how to determine if $executor is shutdown")
    }

    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        throw UnsupportedOperationException("Don't know how to shutdown $executor")
    }

    override fun tryCancel(task: () -> Unit): Boolean = false

    override fun offer(task: () -> Unit): Boolean {
        try {
            executor.execute(task)
            return true
        } catch(e: RejectedExecutionException) {
            return false
        }
    }

}

private class ExecutorServiceDispatcher(private val executor: ExecutorService) :
        ExecutorDispatcher(executor), ExecutorService by executor {
    override fun isTerminated(): Boolean = executor.isTerminated

    override fun isShutdown(): Boolean = executor.isShutdown

    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        if (force) {
            executor.shutdownNow()
            return listOf()
        } else if (block) {
            //I guess waiting a year is enough for peoples patience
            executor.awaitTermination(356, TimeUnit.DAYS)
            return listOf()
        } else if (timeOutMs > 0){
            if(executor.awaitTermination(timeOutMs, TimeUnit.MILLISECONDS)) {
                return listOf()
            } else {
                return executor.shutdownNow().toFunctions()
            }
        }

        return executor.shutdownNow().toFunctions()
    }

    override fun tryCancel(task: () -> Unit): Boolean = false


    private fun List<Runnable>.toFunctions() : List<() -> Unit> = this.map {{it.run()}}
}



private open class DispatcherExecutor(private val dispatcher: Dispatcher) : Executor, Dispatcher by dispatcher {
    override fun execute(command: Runnable) {
        dispatcher.offer { command.run() }
    }
}


private class DispatcherExecutorService(private val dispatcher: Dispatcher) : DispatcherExecutor(dispatcher), ExecutorService {
    private val cancelHandle = WeakRefCancelHandle(dispatcher)

    override fun <T> submit(task: Callable<T>): Future<T> {
        val futureFunction = FutureFunction(cancelHandle, task)
        dispatcher.offer(futureFunction)
        return futureFunction
    }

    override fun <T> submit(task: Runnable, result: T): Future<T> {
        val futureFunction = FutureFunction(cancelHandle, StaticResultCallable(task, result))
        dispatcher.offer(futureFunction)
        return futureFunction
    }

    override fun submit(task: Runnable): Future<*> {
        val futureFunction = FutureFunction(cancelHandle, VoidCallable(task))
        dispatcher.offer(futureFunction)
        return futureFunction
    }

    override fun <T> invokeAny(tasks: MutableCollection<out Callable<T>>): T = invokeAny(tasks, 0, TimeUnit.DAYS)

    override fun <T> invokeAny(tasks: MutableCollection<out Callable<T>>, timeout: Long, unit: TimeUnit): T {

        //Use a copy because if the provided list is modified (in size) this can become a deadlock
        val copy = ArrayList(tasks)
        if (copy.isEmpty()) throw IllegalArgumentException("empty task list")

        val taskCount = AtomicInteger(copy.size)
        val singleLatch = CountDownLatch(1)



        val result = AtomicReference<T>(null)
        val error = AtomicReference<Exception>(null)
        val allFutures = tasks.mapIndexed { idx, task ->
            val function = FutureFunction(cancelHandle, task) {
                self ->
                try {
                    val value = self.get()
                    if( result.compareAndSet(null, value)) {
                        singleLatch.countDown()
                    }
                } catch (e:Exception) {
                    error.compareAndSet(null, e)
                } finally {
                    taskCount.decrementAndGet()
                }
            }
            if (!dispatcher.offer(function)) {
                throw RejectedExecutionException(task.toString())
            }
            function
        }

        val start = System.currentTimeMillis()
        val timeoutMs = TimeUnit.MILLISECONDS.convert(timeout, unit)
        val interval = Math.min(10, timeoutMs)
        fun keepWaiting() = timeoutMs < 1 || System.currentTimeMillis() - start < timeoutMs

        while (singleLatch.count > 0 && taskCount.get() > 0 && keepWaiting()) {
            try {
                singleLatch.await(interval, TimeUnit.MILLISECONDS)
            } catch(e: InterruptedException) {
                //cancel all futures that haven't started
                allFutures.forEach { future -> future.cancel(false) }
                throw e
            }
        }

        //Cancel all
        allFutures.forEach { future -> future.cancel(false) }

        val value = result.get()
        if (value != null) {
            return value
        }

        throw ExecutionException("No task was successful", error.get())
    }

    override fun isTerminated(): Boolean = dispatcher.terminated

    override fun shutdownNow(): MutableList<Runnable> {
        val remains = dispatcher.stop(force = true)
        //kotlin seems to struggle with type inference here, infers Any instead of Runnable.
        val runnables = remains.map<() -> Unit, Runnable> { fn ->
            when (fn) {
                is Runnable -> fn
                is FutureFunction<*> -> when (fn.callable) {
                    is StaticResultCallable<*> -> fn.callable.task
                    is VoidCallable -> fn.callable.task
                    else -> FunctionRunnable(fn)
                }
                else -> FunctionRunnable(fn)
            }

        }
        return runnables.toMutableList()
    }

    override fun isShutdown(): Boolean = dispatcher.stopped

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        val timeOutMs = TimeUnit.MILLISECONDS.convert(timeout, unit)
        val remains = dispatcher.stop(timeOutMs = timeOutMs)
        return remains.isEmpty()
    }

    override fun <T> invokeAll(tasks: MutableCollection<out Callable<T>>): MutableList<Future<T>> = invokeAll(tasks, 0, TimeUnit.DAYS)

    override fun <T> invokeAll(tasks: MutableCollection<out Callable<T>>, timeout: Long, unit: TimeUnit): MutableList<Future<T>> {
        if (tasks.isEmpty()) return ArrayList()

        //Use a copy because if the provided list is modified (in size) this can become a deadlock
        val copy = ArrayList(tasks)

        val latch = CountDownLatch(copy.size)

        //Leveraging a concurrent HashMap for the finished tasks. Using the array index
        //as a key. This way order can be retained of the original list. It's no requirement but is
        //what might be expected
        val finishedFutures = ConcurrentHashMap<Int, Future<T>?>()

        val allFutures = tasks.mapIndexed { idx, task ->
            val function = FutureFunction(cancelHandle, task) {
                self ->
                finishedFutures.put(idx, self)
                latch.countDown()
            }
            if (!dispatcher.offer(function)) {

                throw RejectedExecutionException(task.toString())
            }
            function
        }

        try {
            if (timeout <= 0L) latch.await() else latch.await(timeout, unit)
        } catch(e: InterruptedException) {
            //cancel all futures that haven't started
            allFutures.forEach { future -> future.cancel(false) }
            throw e
        }

        val finished = finishedFutures.entries.sortedBy { entry -> entry.key }.map { entry -> entry.value }

        //Can happen when we are using a timeout on the latch
        if (finished.size < allFutures.size) {
            val toCancel = allFutures.subtract(finished)
            toCancel.forEach { task -> task?.cancel(false) }
        }

        return ArrayList(finished.filterNotNull())

    }

    override fun shutdown() {
        dispatcher.stop(block = false)
    }

}


private interface CancelHandle {
    fun <V> cancel(future: FutureFunction<V>): Boolean
}

//Using weak references avoids that futures that are retained
//are accidentally keeping a whole dispatcher service alive.
private class WeakRefCancelHandle(dispatcher: Dispatcher) : CancelHandle {
    private val reference: Reference<Dispatcher>

    init {
        reference = WeakReference(dispatcher)
    }

    override fun <V> cancel(future: FutureFunction<V>): Boolean {
        val dispatcher = reference.get()
        return if (dispatcher == null) false else dispatcher.tryCancel(future)
    }

}

private class FutureFunction<V>(private val cancelHandle: CancelHandle, val callable: Callable<V>,
                                private val doneFn: (FutureFunction<V>) -> Unit = {}) : Function0<Unit>, Future<V> {
    enum class State {PENDING, SUCCESS, ERROR }

    private @Volatile var state = State.PENDING
    private @Volatile var result: Any? = null
    private @Volatile var queue = 0

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val mutex: Object = Object()


    override fun get(): V = get(0)

    override fun get(timeout: Long, unit: TimeUnit): V = get(TimeUnit.MILLISECONDS.convert(timeout, unit))

    @Suppress("UNREACHABLE_CODE")
    private operator fun get(timeout: Long): V {
        do {
            @Suppress("UNCHECKED_CAST")
            if (state == State.SUCCESS) return result as V
            if (state == State.ERROR) throw result as Exception

            synchronized(mutex) {
                ++queue
                try {
                    while (!isDone) mutex.wait(timeout)
                } finally {
                    --queue
                }
            }
        } while (true)
        throw Exception("unreachable")
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = cancelHandle.cancel(this)

    override fun isDone(): Boolean = state == State.SUCCESS || state == State.ERROR


    override fun isCancelled(): Boolean = false

    override fun invoke() = try {
        setResult(callable.call(), State.SUCCESS)
    } catch (e: Exception) {
        setResult(e, State.ERROR)
    }

    private fun setResult(result: Any?, state: State) {
        this.result = result
        this.state = state
        if (queue > 0) {
            synchronized(mutex) {
                mutex.notifyAll()
            }
        }
        try {
            doneFn(this)
        } catch(e: Exception) {
            //ignore, yes, ignore
        }
    }

}

private class VoidCallable(val task: Runnable) : Callable<Unit?> {
    override fun call(): Unit? {
        task.run()
        return null
    }
}

private class StaticResultCallable<V>(val task: Runnable, private val result: V) : Callable<V> {
    override fun call(): V {
        task.run()
        return result
    }
}

private class FunctionRunnable(val fn: () -> Unit) : Runnable {
    override fun run() = fn()
}


private fun <T> Iterable<T>.toMutableList(): MutableList<T> {
    when (this) {
        is MutableList -> return this
        is Collection<T> -> return ArrayList(this)
    }
    val result = ArrayList<T>()

    this.forEach {
        e ->
        result.add(e)
    }
    return result
}




