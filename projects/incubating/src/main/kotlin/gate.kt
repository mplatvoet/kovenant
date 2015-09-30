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
import java.util.concurrent.atomic.AtomicInteger
import nl.komponents.kovenant.async as baseAsync


public class Gate<V>(val maxConcurrentTasks: Int = 1, val context: Context = Kovenant.context) {
    init {
        if (maxConcurrentTasks < 1)
            throw ConfigurationException("maxConcurrentTasks must be at least 1, but was $maxConcurrentTasks")
    }


    private val concurrentTasks = AtomicInteger(0)
    private val workQueue = NonBlockingWorkQueue<() -> V>()

    public fun async(context: Context = this.context, fn: () -> V): Promise<V, Exception> {
        val taskNumber = concurrentTasks.incrementAndGet()
        val promise = if (taskNumber > maxConcurrentTasks) {

            val deferred = deferred<V, Exception>(context)
            //TODO, queue work

            deferred.promise
        } else {
            baseAsync(context, fn)
        }
        addDonePromise(promise)
        return promise
    }

    public fun <V, E> addDonePromise(promise: Promise<V, E>): Promise<V, E> = promise.always() {
        //TODO, use direct dispatcher bypassing the callback dispatcher

        while (true) {
            val remainingTasks = concurrentTasks.decrementAndGet()
            if (remainingTasks < maxConcurrentTasks && workQueue.isNotEmpty()) {
                val taskNumber = concurrentTasks.incrementAndGet()
                if (taskNumber <= maxConcurrentTasks) {
                    //TODO schedule
                    break
                }
                continue
            }
            break
        }
    }

    private fun scheduleTaskFromQueue(): Boolean {
        return false

    }
}



