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
package nl.komponents.kovenant.android

import android.os.Looper
import nl.komponents.kovenant.Dispatcher
import java.util.ArrayList
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

public enum class DispatcherType {
    FULL BASIC
}


public fun androidUiDispatcher(): Dispatcher = BasicAndroidDispatcher.uiDispatcher

public fun buildLooperDispatcher(looper: Looper,
                                 type: DispatcherType = DispatcherType.BASIC): Dispatcher {
    val executor = LooperExecutor(looper)

    return when (type) {
        DispatcherType.BASIC -> BasicAndroidDispatcher(executor)
        DispatcherType.FULL -> FullAndroidDispatcher(executor)
    }
}




private class BasicAndroidDispatcher(private val looperExecutor: LooperExecutor) : Dispatcher {
    public companion object {
        val uiDispatcher: Dispatcher = BasicAndroidDispatcher(LooperExecutor.main)
    }

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


    private class FnRunnable(private val body: () -> Unit) : Runnable {
        override fun run() = body()
    }
}

private class FullAndroidDispatcher(private val looperExecutor: LooperExecutor) : Dispatcher {
    private val queue = ConcurrentLinkedQueue<ControllableFnRunnable>()
    private val unfinishedCount = AtomicInteger(0)

    private val running = AtomicBoolean(true)

    override fun offer(task: () -> Unit): Boolean {
        if (!running.get()) return false;

        val trackingId = looperExecutor.claimTrackingId()
        val runnable = ControllableFnRunnable(trackingId, task)
        queue add runnable
        unfinishedCount.incrementAndGet()
        looperExecutor.submit(runnable, trackingId)

        return true

    }

    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        if (running.compareAndSet(true, false)) {
            if (block) {
                val localTimeOutMs = if (force) 1 else timeOutMs
                val now = System.currentTimeMillis()
                val napTimeMs = Math.min(timeOutMs, 10L)

                fun allJobsDone() = unfinishedCount.get() <= 0
                fun keepWaiting() = localTimeOutMs < 1 || (System.currentTimeMillis() - now) >= localTimeOutMs

                var interrupted = false
                while (!allJobsDone() && keepWaiting()) {
                    try {
                        Thread.sleep(napTimeMs)
                    } catch (e: InterruptedException) {
                        //ignoring for now since it would break the shutdown contract
                        //remember and interrupt later
                        interrupted = true
                    }
                }
                if (interrupted) {
                    //calling thread was interrupted during shutdown, set the interrupted flag again
                    Thread.currentThread().interrupt()
                }
                return drainAll()
            }
        }
        return ArrayList() //drainAll() also returns an ArrayList, returning this for consistency
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

    override val terminated: Boolean
        get() = stopped && unfinishedCount.get() == 0

    override val stopped: Boolean
        get() = running.get()

    private inner class ControllableFnRunnable(
            val trackingId: Int,
            val body: () -> Unit) : Runnable {

        override fun run() {
            if (queue remove this) {
                //only execute if we are the one removing ourselves from the queue
                //otherwise this job has been cancelled
                body()

                unfinishedCount.decrementAndGet()
            }
        }
    }
}




