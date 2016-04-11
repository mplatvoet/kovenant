/*
 * Copyright (c) 2016 Mark Platvoet<mplatvoet@gmail.com>
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

package nl.komponents.kovenant.incubating.dispatcher

import nl.komponents.kovenant.unsafe.UnsafeAtomicReferenceFieldUpdater
import nl.komponents.kovenant.unsafe.hasUnsafe
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

class StickyThreadBlockingQueue<E : Any> {
    private @Volatile var head: QueueNode<E> = QueueNode()

    fun pop(thread: Thread = Thread.currentThread()): E {
        while (true) {
            var current = head
            while (true) {
                //no work for this thread, thus break out
                current = current.next ?: break

                if (current.thread == thread) {
                    if (current.isRead()) {
                        //TODO shift head if necessary

                        continue
                    }
                    return popNode(current)

                }

                if (current.thread == null && current.trySetThread(thread)) {
                    return popNode(current)
                }

            }
            TODO("should block here")
        }
    }

    //assumes this node contains the proper thread
    private fun popNode(node: QueueNode<E>): E {
        val value = node.unsafeGetValue()
        if (head.next == node) {
            head = node
        } else {
            node.markAsRead()
        }
        return value
    }


    fun push(element: E, thread: Thread?) {
        val node = detachedNodeFor(element, thread)
        appendNode(node)
    }

    private fun appendNode(node: QueueNode<E>) {
        do {
            //TODO, optimize tail finding. e.g. use second node for cached tail
            val tail = head.findTail()
        } while (!tail.trySetNext(node))
    }

    private fun detachedNodeFor(element: E, thread: Thread?) = QueueNode<E>().apply {
        setValueLazy(element)
        setThreadLazy(thread)
    }
}


class QueueNode<E : Any> {
    companion object {
        private val nextUpdater: AtomicReferenceFieldUpdater<QueueNode<*>, QueueNode<*>>
        private val threadUpdater: AtomicReferenceFieldUpdater<QueueNode<*>, Thread>

        private val read = Any()

        init {
            if (hasUnsafe()) {
                nextUpdater = UnsafeAtomicReferenceFieldUpdater(QueueNode::class, "_next")
                threadUpdater = UnsafeAtomicReferenceFieldUpdater(QueueNode::class, "_thread")
            } else {
                nextUpdater = AtomicReferenceFieldUpdater.newUpdater(QueueNode::class.java, QueueNode::class.java, "_next")
                threadUpdater = AtomicReferenceFieldUpdater.newUpdater(QueueNode::class.java, Thread::class.java, "_thread")
            }
        }
    }


    private @Volatile var _value: Any? = null
    private @Volatile var _next: QueueNode<E>? = null
    private @Volatile var _thread: Thread? = null

    val next: QueueNode<E>? get() = _next

    val thread: Thread? get() = _thread

    fun isRead() = _value == read

    fun markAsRead() {
        _value = read
    }

    @Suppress("UNCHECKED_CAST")
    fun unsafeGetValue(): E = _value as E

    fun setValueLazy(newValue: E) {
        _value = newValue
    }

    fun setThreadLazy(newThread: Thread?) {
        _thread = newThread
    }

    fun trySetThread(thread: Thread) = threadUpdater.compareAndSet(this, null, thread)

    fun trySetNext(node: QueueNode<E>) = nextUpdater.compareAndSet(this, null, node)
}

private fun <E : Any> QueueNode<E>.findTail(): QueueNode<E> {
    var current = this
    while (true) current = current.next ?: return current
}