/*
 * Copyright (c) 2014-2015 Mark Platvoet<mplatvoet@gmail.com>
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package nl.mplatvoet.komponents.kovenant

import java.util.ArrayList
import java.util.concurrent.*


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

private data open class ExecutorDispatcher(private val executor: Executor) : Dispatcher, Executor by executor {
    override fun isTerminated(): Boolean {
        throw UnsupportedOperationException("Don't know how to determine if $executor is terminated")
    }

    override fun isShutdown(): Boolean {
        throw UnsupportedOperationException("Don't know how to determine if $executor is shutdown")
    }

    override fun shutdown(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        throw UnsupportedOperationException("Don't know how to shutdown $executor")
    }

    override fun cancel(task: () -> Unit): Boolean = false

    override fun submit(task: () -> Unit): Boolean {
        try {
            executor.execute(task)
            return true
        } catch(e: RejectedExecutionException) {
            return false
        }
    }
}

private data class ExecutorServiceDispatcher(private val executor: ExecutorService) :
        ExecutorDispatcher(executor), ExecutorService by executor

private data open class DispatcherExecutor(private val dispatcher: Dispatcher) : Executor, Dispatcher by dispatcher {
    override fun execute(command: Runnable) {
        dispatcher.submit { command.run() }
    }
}


private data class DispatcherExecutorService(private val dispatcher: Dispatcher) : DispatcherExecutor(dispatcher), ExecutorService {
    override fun <T> submit(task: Callable<T>): Future<T> {
        val futureFunction = FutureFunction(task)
        dispatcher.submit(futureFunction)
        return futureFunction
    }

    override fun <T> submit(task: Runnable, result: T): Future<T> {
        val futureFunction = FutureFunction(StaticResultCallable(task, result))
        dispatcher.submit(futureFunction)
        return futureFunction
    }

    override fun submit(task: Runnable): Future<*> {
        val futureFunction = FutureFunction(VoidCallable(task))
        dispatcher.submit(futureFunction)
        return futureFunction
    }

    override fun <T> invokeAny(tasks: MutableCollection<out Callable<T>>): T {
        throw UnsupportedOperationException()
    }

    override fun <T> invokeAny(tasks: MutableCollection<out Callable<T>>, timeout: Long, unit: TimeUnit): T {
        throw UnsupportedOperationException()
    }

    override fun isTerminated(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun shutdownNow(): MutableList<Runnable> {
        val remains = dispatcher.shutdown(force = true)
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

    override fun isShutdown(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
        val timeOutMs = TimeUnit.MILLISECONDS.convert(timeout, unit)
        val remains = dispatcher.shutdown(timeOutMs = timeOutMs)
        return remains.isEmpty()
    }

    override fun <T> invokeAll(tasks: MutableCollection<out Callable<T>>): MutableList<Future<T>> {
        throw UnsupportedOperationException()

    }

    override fun <T> invokeAll(tasks: MutableCollection<out Callable<T>>, timeout: Long, unit: TimeUnit): MutableList<Future<T>> {
        if (tasks.isEmpty()) return ArrayList()

        //Use a copy because if the provided list is modified (in size) this can become a deadlock
        val copy = ArrayList(tasks)

        val latch = CountDownLatch(copy.size())

        //Create a new list for completed finishedFutures and initialize with null.
        //Retaining order of the original list. This is no prerequisite of the interface
        //but is what might be expected.
        val finishedFutures = ConcurrentHashMap<Int, Future<T>?>()

        val allFutures = tasks mapIndexed { idx, task ->
            val function = FutureFunction(task) {
                self ->
                finishedFutures.put(idx, self)
            }
            dispatcher.submit(function)
            function
        }

        try {
            if (timeout <= 0L) latch.await() else latch.await(timeout, unit)
        } catch(e: InterruptedException) {
            //cancel all futures that haven't started
            allFutures forEach { future -> future.cancel(false) }
            throw e
        }

        val finished = finishedFutures.entrySet() sortBy { entry -> entry.key } map { entry -> entry.value }
        return ArrayList(finished)

    }

    override fun shutdown() {
        dispatcher.shutdown(block = false)
    }

}

private class FutureFunction<V>(val callable: Callable<V>,
                                private val doneFn: (FutureFunction<V>) -> Unit = {}) : Function0<Unit>, Future<V> {
    enum class State {PENDING SUCCESS ERROR }

    private volatile var state = State.PENDING
    private volatile var result: Any? = null
    private volatile var queue = 0

    [suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")]
    private val mutex: Object = Object()


    override fun get(): V = get(0)

    override fun get(timeout: Long, unit: TimeUnit): V = get(TimeUnit.MILLISECONDS.convert(timeout, unit))

    [suppress("UNREACHABLE_CODE")]
    private fun get(timeout: Long): V {
        do {
            [suppress("UNCHECKED_CAST")]
            when (state) {
                State.SUCCESS -> return result as V
                State.ERROR -> throw result as Exception
            }

            synchronized(mutex) {
                ++queue
                try {
                    while (!isDone()) mutex.wait(timeout)
                } finally {
                    --queue
                }
            }
        } while (true)
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false

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

    this forEach {
        e ->
        result.add(e)
    }
    return result
}




