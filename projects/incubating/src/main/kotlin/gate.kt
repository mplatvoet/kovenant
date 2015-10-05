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

package nl.komponents.kovenant.incubating

import nl.komponents.kovenant.*
import java.util.concurrent.Semaphore
import nl.komponents.kovenant.async as baseAsync


public class Gate(val maxConcurrentTasks: Int = 1, public val context: Context = Kovenant.context) {
    private val semaphore = Semaphore(maxConcurrentTasks)

    init {
        if (maxConcurrentTasks < 1)
            throw ConfigurationException("maxConcurrentTasks must be at least 1, but was $maxConcurrentTasks")
    }

    private val workQueue = NonBlockingWorkQueue<Task<*, *>>()

    public fun <V> async(context: Context = this.context, fn: () -> V): Promise<V, Exception> {

        val promise = if (semaphore.tryAcquire()) {
            baseAsync(context, fn)

        } else {
            val asyncTask = AsyncTask(context, fn)
            workQueue.offer(asyncTask)

            //Could be missed, try acquire again
            if (semaphore.tryAcquire()) {
                workQueue.poll()?.schedule()
            }
            asyncTask.promise
        }
        addDonePromise(promise)
        return promise
    }

    private fun <V, E> addDonePromise(promise: Promise<V, E>): Promise<V, E> = promise.always() {
        //TODO, use direct dispatcher bypassing the callback dispatcher
        semaphore.release()
        if (workQueue.isNotEmpty() && semaphore.tryAcquire()) {
            workQueue.poll()?.schedule()
        }
    }

}

private interface Task<V, E> {
    val promise: Promise<V, E>
    fun schedule()
}

private class AsyncTask<V>(private val context: Context, private val fn: () -> V) : Task<V, Exception> {
    private val deferred: Deferred<V, Exception> = deferred(context)
    override val promise: Promise<V, Exception>
        get() = deferred.promise

    override fun schedule() {
        //TODO, use direct dispatcher bypassing the callback dispatcher
        baseAsync(context, fn).success {
            deferred.resolve(it)
        }.fail {
            deferred.reject(it)
        }
    }

}



