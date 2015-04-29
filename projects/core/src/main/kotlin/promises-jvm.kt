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

package nl.mplatvoet.komponents.kovenant

import java.util.concurrent.atomic.AtomicReference

private class DeferredPromise<V, E>(override val context: Context) : Promise<V, E>, Deferred<V, E>, ContextAware {

    private val head = AtomicReference<CallbackContextNode<V, E>>(null)
    private val state = AtomicReference(State.PENDING)
    private volatile var result: Any? = null


    override fun resolve(value: V) {
        if (trySetSuccessResult(value)) {
            fireSuccess(value)
        } else {
            throw IllegalStateException("Promise already resolved")
        }
    }

    override fun reject(error: E) {
        if (trySetFailResult(error)) {
            fireFail(error)
        } else {
            throw IllegalStateException("Promise already resolved")
        }
    }

    override val promise: Promise<V, E> = this


    override fun success(callback: (value: V) -> Unit): Promise<V, E> {
        if (isFailResult()) return this

        addSuccessCb(callback)

        //possibly resolved already
        if (isSuccessResult()) fireSuccess(getAsValueResult())

        return this
    }

    override fun fail(callback: (error: E) -> Unit): Promise<V, E> {
        if (isSuccessResult()) return this

        addFailCb(callback)

        //possibly rejected already
        if (isFailResult()) fireFail(getAsFailResult())

        return this
    }

    override fun always(callback: () -> Unit): Promise<V, E> {
        addAlwaysCb(callback)

        //possibly completed already
        when {
            isSuccessResult() -> fireSuccess(getAsValueResult())
            isFailResult() -> fireFail((getAsFailResult()))
        }

        return this
    }


    private fun fireSuccess(value: V) = popAllSuccessAndAlways {
        node ->
        context.tryDispatch {
            node.runSuccess(value)
        }
    }

    private fun fireFail(value: E) = popAllFailAndAlways {
        node ->
        context.tryDispatch {
            node.runFail(value)
        }
    }

    private enum class State {
        PENDING
        MUTATING
        SUCCESS
        FAIL
    }

    private enum class NodeState {
        CHAINED
        POPPING
        APPENDING
    }


    fun trySetSuccessResult(result: V): Boolean {
        if (state.get() != State.PENDING) return false
        if (state.compareAndSet(State.PENDING, State.MUTATING)) {
            this.result = result
            state.set(State.SUCCESS)
            return true
        }
        return false
    }

    fun trySetFailResult(result: E): Boolean {

        if (state.get() != State.PENDING) return false
        if (state.compareAndSet(State.PENDING, State.MUTATING)) {
            this.result = result
            state.set(State.FAIL)
            return true
        }
        return false
    }

    private fun isSuccessResult(): Boolean = state.get() == State.SUCCESS
    private fun isFailResult(): Boolean = state.get() == State.FAIL


    //For internal use only! Method doesn't check anything, just casts.
    suppress("UNCHECKED_CAST")
    private fun getAsValueResult(): V = result as V

    //For internal use only! Method doesn't check anything, just casts.
    suppress("UNCHECKED_CAST")
    private fun getAsFailResult(): E = result as E


    private fun addSuccessCb(cb: (V) -> Unit) = addValueNode(SuccessCallbackContextNode<V, E>(cb))

    private fun addFailCb(cb: (E) -> Unit) = addValueNode(FailCallbackContextNode<V, E>(cb))

    private fun addAlwaysCb(cb: () -> Unit) = addValueNode(AlwaysCallbackContextNode<V, E>(cb))

    private inline fun popAllSuccessAndAlways(fn: (CallbackContext<V, E>) -> Unit) {
        popAll {
            if (it is SuccessCallbackContextNode || it is AlwaysCallbackContextNode) {
                fn(it)
            }
        }
    }

    private inline fun popAllFailAndAlways(fn: (CallbackContext<V, E>) -> Unit) {
        popAll {
            if (it is FailCallbackContextNode || it is AlwaysCallbackContextNode) {
                fn(it)
            }
        }
    }

    private fun createHeadNode(): CallbackContextNode<V, E> = EmptyCallbackContextNode()

    private fun addValueNode(node: CallbackContextNode<V, E>) {
        //ensure there is a head
        ensureHeadNode()

        while (true) {
            val tail = findTailNode()

            if (tail.nodeState.compareAndSet(NodeState.CHAINED, NodeState.APPENDING)) {
                if (tail.next == null) {
                    tail.next = node
                    tail.nodeState.set(NodeState.CHAINED)
                    return
                }
                tail.nodeState.set(NodeState.CHAINED)
            }
        }
    }

    private fun ensureHeadNode() {
        while (head.get() == null) {
            head.compareAndSet(null, createHeadNode())
        }
    }

    private fun findTailNode(): CallbackContextNode<V, E> {
        var tail = head.get()
        while (true) {
            val next = tail.next
            if (next == null) {
                return tail
            }
            tail = next
        }
    }


    private inline fun popAll(fn: (CallbackContext<V, E>) -> Unit) {
        val localHead = head.get()

        if (localHead != null) {
            do {
                val popper = localHead.next
                if (popper != null) {
                    if (localHead.nodeState.compareAndSet(NodeState.CHAINED, NodeState.POPPING)) {
                        if (popper.nodeState.compareAndSet(NodeState.CHAINED, NodeState.POPPING)) {
                            localHead.next = popper.next
                            localHead.nodeState.set(NodeState.CHAINED)
                            popper.next = null
                            fn(popper)
                        }
                        localHead.nodeState.set(NodeState.CHAINED)
                    }
                }
            } while (popper != null)
        }
    }

    private trait CallbackContext<V, E> {
        public fun runSuccess(value: V)

        public fun runFail(value: E)
    }

    private abstract class CallbackContextNode<V, E> : CallbackContext<V, E> {
        volatile var next: CallbackContextNode<V, E>? = null
        var nodeState = AtomicReference(NodeState.CHAINED)
    }

    private class EmptyCallbackContextNode<V, E> : CallbackContextNode<V, E>() {

        override fun runSuccess(value: V) {
            //ignore
        }

        override fun runFail(value: E) {
            //ignore
        }
    }

    private class AlwaysCallbackContextNode<V, E>(private val fn: () -> Unit) : CallbackContextNode<V, E>() {
        override fun runSuccess(value: V) = fn()

        override fun runFail(value: E) = fn()
    }

    private class SuccessCallbackContextNode<V, E>(private val fn: (V) -> Unit) : CallbackContextNode<V, E>() {

        override fun runSuccess(value: V) = fn(value)

        override fun runFail(value: E) {
            //ignore
        }
    }

    private class FailCallbackContextNode<V, E>(private val fn: (E) -> Unit) : CallbackContextNode<V, E>() {

        override fun runSuccess(value: V) {
            //ignore
        }

        override fun runFail(value: E) = fn(value)
    }

}






