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
@file:JvmName("KovenantJvmThrottle")
package nl.komponents.kovenant.jvm

import nl.komponents.kovenant.*
import java.util.concurrent.Semaphore
import nl.komponents.kovenant.task as baseTask

/**
 * A Throttle restricts the number of concurrent tasks that are being executed. This runs on top of
 * existing Dispatchers. This is useful for situations where some Promises represent large amount of memory.
 *
 * @constructor maxConcurrentTasks set the maximum of parallel processes, must be at least 1
 * @constructor context the default context on which all tasks operate
 */
public class Throttle(val maxConcurrentTasks: Int = 1, val context: Context = Kovenant.context) {
    private val semaphore = Semaphore(maxConcurrentTasks)

    init {
        if (maxConcurrentTasks < 1)
            throw ConfigurationException("maxConcurrentTasks must be at least 1, but was $maxConcurrentTasks")
    }

    private val workQueue = NonBlockingWorkQueue<Task>()


    /**
     * Registers an async task to be executed somewhere in the near future. Callers must ensure that they call
     * registerDone() with some promise that signals this process is done.
     */
    public fun <V> registerTask(context: Context = this.context, fn: () -> V): Promise<V, Exception> {
        if (semaphore.tryAcquire()) {
            if (workQueue.isEmpty()) {
                return baseTask(context, fn)
            }
            semaphore.release()
        }


        val asyncTask = AsyncTask(context, fn)
        workQueue.offer(asyncTask)
        val promise = asyncTask.promise

        tryScheduleTasks()
        return promise
    }

    /**
     * Registers an async task to be executed somewhere in the near future. When this task is done the process is
     * considered to be finished and other tasks are allowed to execute.
     */
    public fun <V> task(context: Context = this.context, fn: () -> V): Promise<V, Exception> {
        return registerTask(context, fn).addDone()
    }


    /**
     * Registers an async task to be executed somewhere in the near future based on an existing Promise.
     * Callers must ensure that they call registerDone() with some promise that signals this process is done.
     */
    public fun <V, R> registerTask(promise: Promise<V, Exception>,
                                   context: Context = promise.context,
                                   fn: (V) -> R): Promise<R, Exception> {
        if (promise.isDone() && workQueue.isEmpty() && semaphore.tryAcquire()) {
            return promise.then(context, fn)
        }

        val deferred = deferred<R, Exception>(context)

        if (promise.isDone()) {
            workQueue.offer(ThenTask(promise, deferred, fn))
            tryScheduleTasks()
        } else {
            promise.success(DirectDispatcherContext) {
                workQueue.offer(ThenTask(promise, deferred, fn))
                tryScheduleTasks()
            }.fail(DirectDispatcherContext) {
                //also schedule fails to maintain order
                workQueue.offer(ThenTask(promise, deferred, fn))
                tryScheduleTasks()
            }
        }

        return deferred.promise
    }

    /**
     * Registers an async task to be executed somewhere in the near future based on a existing Promise.
     * When this task is done the process is considered to be finished and other tasks are allowed to execute.
     */
    public fun <V, R> task(promise: Promise<V, Exception>,
                           context: Context = promise.context,
                           fn: (V) -> R): Promise<R, Exception> {
        return registerTask(promise, context, fn).addDone()
    }

    /**
     * Register a promise that signals that a previous registered task has finished
     */
    public fun <V, E> registerDone(promise: Promise<V, E>): Promise<V, E> = promise.addDone()

    private fun <V, E> Promise<V, E>.addDone(): Promise<V, E> = this.always(DirectDispatcherContext) {
        semaphore.release()
        tryScheduleTasks()
    }


    private fun tryScheduleTasks() {
        while (workQueue.isNotEmpty() && semaphore.tryAcquire()) {
            val task = workQueue.poll()
            if (task != null) {
                task.schedule()
            } else {
                semaphore.release()
            }
        }
    }

}

private interface Task {
    fun schedule()
}

private class AsyncTask<V>(private val context: Context, private val fn: () -> V) : Task {
    private val deferred: Deferred<V, Exception> = deferred(context)
    val promise: Promise<V, Exception>
        get() = deferred.promise

    override fun schedule() {
        baseTask(context, fn).success(DirectDispatcherContext) {
            deferred.resolve(it)
        }.fail(DirectDispatcherContext) {
            deferred.reject(it)
        }
    }
}

private class ThenTask<V, R>(private val promise: Promise<V, Exception>,
                             private val deferred: Deferred<R, Exception>,
                             private val fn: (V) -> R) : Task {
    override fun schedule() {
        if (!promise.isDone()) throw KovenantException("state exception: promise should be done")

        if (promise.isFailure()) {
            deferred.reject(promise.getError())
        } else {
            baseTask(deferred.promise.context) { fn(promise.get()) }.success(DirectDispatcherContext) {
                deferred.resolve(it)
            }.fail(DirectDispatcherContext) {
                deferred.reject(it)
            }
        }
    }
}