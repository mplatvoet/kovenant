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

package nl.mplatvoet.kotlin.komponents.promises

import java.util.concurrent.atomic.AtomicReference

/**
 * Created by mark on 28/09/14.
 *
 * Bridge functions to work with the java ValueNode.class.
 * It's basically a very lightweight non blocking linked list implementation.
 * I'm trying to keep the memory footprint as low as possible.
 *
 * Using an atomic reference as base container.
 */

/* Using an iteration function that can be inlined instead of creating Iterators.
 *
 */
private inline fun <T> AtomicReference<ValueNode<T>>.iterate(cb: (ValueNode<T>) -> Unit) {
    var node = get()
    while (node != null) {
        val n = node
        cb(n)
        node = n.next
    }
}

private fun <T> AtomicReference<ValueNode<T>>.add(value: T) {
    val node = ValueNode(value)

    if (!this.compareAndSet(null, node)) {
        //when this fails it means it already contained a node
        //we can safely append
        get().append(node)
    }
}

private val <T:Any> ValueNode<T>.next : ValueNode<T>? get() = this.getNext()
private val <T:Any> ValueNode<T>.done : Boolean get() = this.isDone()
private val <T:Any> ValueNode<T>.value: T get() = this.getValue()