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

class StickyThreadQueue<E : Any> {
    //    companion object {
    //        private val headRefUpdater = AtomicReferenceFieldUpdater.newUpdater(StickyThreadQueue::class.java, QueueNode::class.java, "_head")
    //    }

    private @Volatile var head: QueueNode<E> = QueueNode()

    fun add(element: E, thread: Thread?) {
        val node = detachedNodeFor(element, thread)
        appendNode(node)
    }

    private fun appendNode(node: QueueNode<E>) {

    }

    private fun detachedNodeFor(element: E, thread: Thread?) = QueueNode<E>().apply {
        setValueLazy(element)
        setThreadLazy(thread)
    }
}


class QueueNode<E : Any> {
    private @Volatile var _value: E? = null
    private @Volatile var _next: QueueNode<E>? = null
    private @Volatile var _thread: Thread? = null

    val next: QueueNode<E>? get() = _next

    fun setValueLazy(newValue: E) {
        _value = newValue
    }

    fun setThreadLazy(newThread: Thread?) {
        _thread = newThread
    }
}


inline fun <E : Any> QueueNode<E>.stream(body: (QueueNode<E>) -> Unit) {
    var current = this
    while (true) {
        body(current)
        val next = current.next
        if (next != null) {
            current = next
        } else {
            break
        }
    }
}

