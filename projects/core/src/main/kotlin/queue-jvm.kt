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

package nl.komponents.kovenant

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class NonBlockingWorkQueue<V : Any>() : BlockingSupportWorkQueue<V>() {
    private val queue = ConcurrentLinkedQueue<V>()

    override fun tryPoll(): V? = queue.poll()
    override fun tryOffer(elem: V): Boolean = queue.offer(elem)

    override fun size(): Int = queue.size

    override fun isEmpty(): Boolean = queue.isEmpty()
    override fun isNotEmpty(): Boolean = !isEmpty()

    override fun remove(elem: Any?): Boolean = queue.remove(elem)
}

abstract class BlockingSupportWorkQueue<V : Any>() : WorkQueue<V> {
    private val waitingThreads = AtomicInteger(0)

    //yes I could also use a ReentrantLock with a Condition but
    //introduces quite a lot of overhead and the semantics
    //are just the same
    private val mutex = Object()

    abstract fun tryPoll(): V?

    abstract fun tryOffer(elem: V): Boolean

    override fun offer(elem: V): Boolean {
        val added = tryOffer(elem)

        if (added && waitingThreads.get() > 0) {
            synchronized(mutex) {
                //maybe there aren't any threads waiting or
                //there isn't anything in the queue anymore
                //just notify, we've got this far
                mutex.notifyAll()
            }
        }

        return added
    }

    override fun poll(block: Boolean, timeoutMs: Long): V? {
        if (!block) return tryPoll()

        val elem = tryPoll()
        if (elem != null) return elem

        waitingThreads.incrementAndGet()
        try {
            return if (timeoutMs > -1L) {
                blockingPoll(timeoutMs)
            } else {
                blockingPoll()
            }
        } finally {
            waitingThreads.decrementAndGet()
        }
    }


    private fun blockingPoll(): V? {
        synchronized(mutex) {
            while (true) {
                val retry = tryPoll()
                if (retry != null) return retry
                mutex.wait()
            }
        }
        throw KovenantException("unreachable")
    }

    private fun blockingPoll(timeoutMs: Long): V? {
        val deadline = System.currentTimeMillis() + timeoutMs
        synchronized(mutex) {
            while (true) {
                val retry = tryPoll()
                if (retry != null || System.currentTimeMillis() >= deadline) return retry
                mutex.wait(timeoutMs)
            }
        }
        throw KovenantException("unreachable")
    }
}

