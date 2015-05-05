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
package nl.mplatvoet.komponents.kovenant.android

import android.os.Looper
import nl.mplatvoet.komponents.kovenant.Dispatcher
import java.util.ArrayList
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

public fun androidUIDispatcher(): Dispatcher = looperDispatcher(Looper.getMainLooper())

public fun looperDispatcher(looper: Looper,
                            fullDispatcher: Boolean = false): Dispatcher {
    val executor = LooperExecutor(looper)
    return if (fullDispatcher) FullAndroidDispatcher(executor) else BasicAndroidDispatcher(executor)
}

private class BasicAndroidDispatcher(private val looperExecutor: LooperExecutor) : Dispatcher {

    override fun offer(task: () -> Unit): Boolean {
        looperExecutor.submit(FnRunnable(task))
        return true
    }

    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> = throw UnsupportedOperationException()
    override fun tryCancel(task: () -> Unit): Boolean = throw UnsupportedOperationException()

    override val terminated: Boolean
        get() = throw UnsupportedOperationException()
    override val stopped: Boolean
        get() = throw UnsupportedOperationException()

}

private class FullAndroidDispatcher(private val looperExecutor: LooperExecutor) : Dispatcher {
    private val queue = ConcurrentLinkedQueue<ControllableFnRunnable>()

    private volatile var running = true

    override fun offer(task: () -> Unit): Boolean {
        if (!running) return false;

        val trackingId = looperExecutor.claimTrackingId()
        val runnable = ControllableFnRunnable(queue, trackingId, task)
        queue add runnable
        looperExecutor.submit(runnable, trackingId)

        return true

    }

    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        running = false

        // TODO also implement waiting for all jobs to finish

        return drainAll()
    }

    private fun drainAll(): List<() -> Unit> {
        val cancelled = ArrayList<() -> Unit>(queue.size())
        queue.pollEach {
            cancelled add it.body
            looperExecutor tryRemove it.trackingId
        }
        return cancelled
    }

    private inline fun Queue<ControllableFnRunnable>.pollEach(fn: (ControllableFnRunnable) -> Unit) {
        do {
            val item = poll()
            if (item != null) {
                fn(item)
            }
        } while (item != null)
    }

    override fun tryCancel(task: () -> Unit): Boolean {
        val controllable = findControllableForFn(task)
        if ( controllable != null && queue remove controllable ) {
            looperExecutor.tryRemove(controllable.trackingId)
            return true
        }
        return false
    }

    private fun findControllableForFn(task: () -> Unit): ControllableFnRunnable? {
        queue forEach {
            if (it.body == task) {
                return it
            }
        }
        return null
    }

    //TODO not correct, jobs can still be running
    override val terminated: Boolean
        get() = stopped && queue.isEmpty()

    override val stopped: Boolean
        get() = running

}

private class ControllableFnRunnable(private val queue: Queue<ControllableFnRunnable>,
                                     val trackingId: Int,
                                     val body: () -> Unit) : Runnable {
    override fun run() {
        if (queue remove this) {
            //only execute if we are the one removing ourselves from the queue
            //otherwise this job has been cancelled
            body()
        }
    }

}


private class FnRunnable(private val body: () -> Unit) : Runnable {
    override fun run() = body()
}


