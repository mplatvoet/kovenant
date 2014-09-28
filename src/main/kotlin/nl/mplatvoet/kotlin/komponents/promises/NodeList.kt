package nl.mplatvoet.kotlin.komponents.promises

import java.util.concurrent.atomic.AtomicReference

/**
 * Created by mark on 28/09/14.
 *
 * Bridge functions to work with the java Node.class.
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
        val n = node as ValueNode<T>
        cb(n)
        node = n.next
    }
}

private fun <T> AtomicReference<ValueNode<T>>.add(value: T) {
    val node = ValueNode(value)

    if (!this.compareAndSet(null, node)) {
        //when this fails it means it already contained a node
        //we can safely append
        get()!!.append(node)
    }
}

private val <T:Any> ValueNode<T>.next : ValueNode<T>? get() = this.getNext()
private val <T:Any> ValueNode<T>.done : Boolean get() = this.isDone()
private val <T:Any> ValueNode<T>.value: T get() = this.getValue() as T