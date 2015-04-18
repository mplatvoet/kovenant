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

package nl.mplatvoet.komponents.kovenant;

import kotlin.Function1;
import kotlin.Unit;

abstract class AbstractPromise<V, E> {
    private enum State {PENDING, MUTATING, SUCCESS, FAIL}

    private enum NodeState {CHAINED, POPPING, APPENDING}

    private static final long successCbsOffset = UnsafeAccess.objectFieldOffset(AbstractPromise.class, "successCbs");
    private static final long failCbsOffset = UnsafeAccess.objectFieldOffset(AbstractPromise.class, "failCbs");
    private static final long alwaysCbsOffset = UnsafeAccess.objectFieldOffset(AbstractPromise.class, "alwaysCbs");

    private static final long stateOffset = UnsafeAccess.objectFieldOffset(AbstractPromise.class, "state");


    private static final long nodeStateOffset = UnsafeAccess.objectFieldOffset(ValueNode.class, "nodeState");


    protected volatile ValueNode<kotlin.Function1<V, kotlin.Unit>> successCbs = null;
    protected volatile ValueNode<kotlin.Function1<E, kotlin.Unit>> failCbs = null;
    protected volatile ValueNode<kotlin.Function0<kotlin.Unit>> alwaysCbs = null;

    private volatile State state = State.PENDING;
    private volatile Object result = null;


    private static final Object EMPTY = new Object();

    @SuppressWarnings("unchecked")
    private static <T> ValueNode<T> createHeadNode() {
        return (ValueNode<T>) new ValueNode(EMPTY);
    }

    boolean trySetSuccessResult(V result) {
        if (state != State.PENDING) return false;
        if (UnsafeAccess.compareAndSwapObject(this, stateOffset, State.PENDING, State.MUTATING)) {
            this.result = result;
            state = State.SUCCESS;
            return true;
        }
        return false;
    }

    boolean trySetFailResult(E result) {

        if (state != State.PENDING) return false;
        if (UnsafeAccess.compareAndSwapObject(this, stateOffset, State.PENDING, State.MUTATING)) {
            this.result = result;
            state = State.FAIL;
            return true;
        }
        return false;
    }


    boolean isSuccessResult() {
        return state == State.SUCCESS;
    }

    boolean isFailResult() {
        return state == State.FAIL;
    }

    boolean isCompleted() {
        return state == State.SUCCESS || state == State.FAIL;
    }

    /*
    For internal use only! Method doesn't check anything, just casts.
     */
    @SuppressWarnings("unchecked")
    V getAsValueResult() {
        return (V) result;
    }

    /*
    For internal use only! Method doesn't check anything, just casts.
     */
    @SuppressWarnings("unchecked")
    E getAsFailResult() {
        return (E) result;
    }


    void addSuccessCb(kotlin.Function1<V, kotlin.Unit> cb) {
        ValueNode<kotlin.Function1<V, kotlin.Unit>> node = new ValueNode<Function1<V, Unit>>(cb);
        //ensure there is a head
        while (successCbs == null) {
            UnsafeAccess.compareAndSwapObject(this, successCbsOffset, null, createHeadNode());
        }

        while (true) {
            ValueNode<kotlin.Function1<V, kotlin.Unit>> tail = successCbs;
            while (tail.next != null) tail = tail.next;
            if (UnsafeAccess.compareAndSwapObject(tail, nodeStateOffset, NodeState.CHAINED, NodeState.APPENDING)) {
                tail.next = node;
                tail.nodeState = NodeState.CHAINED;
                return;
            }
        }
    }

    void addFailCb(kotlin.Function1<E, kotlin.Unit> cb) {
        ValueNode<kotlin.Function1<E, kotlin.Unit>> node = new ValueNode<Function1<E, Unit>>(cb);
        //ensure there is a head
        while (failCbs == null) {
            UnsafeAccess.compareAndSwapObject(this, failCbsOffset, null, createHeadNode());
        }

        while (true) {
            ValueNode<kotlin.Function1<E, kotlin.Unit>> tail = failCbs;
            while (tail.next != null) tail = tail.next;
            if (UnsafeAccess.compareAndSwapObject(tail, nodeStateOffset, NodeState.CHAINED, NodeState.APPENDING)) {
                tail.next = node;
                tail.nodeState = NodeState.CHAINED;
                return;
            }
        }
    }

    void addAlwaysCb(kotlin.Function0<kotlin.Unit> cb) {
        ValueNode<kotlin.Function0<kotlin.Unit>> node = new ValueNode<kotlin.Function0<Unit>>(cb);
        //ensure there is a head
        while (alwaysCbs == null) {
            UnsafeAccess.compareAndSwapObject(this, alwaysCbsOffset, null, createHeadNode());
        }

        while (true) {
            ValueNode<kotlin.Function0<kotlin.Unit>> tail = alwaysCbs;
            while (tail.next != null) tail = tail.next;
            if (UnsafeAccess.compareAndSwapObject(tail, nodeStateOffset, NodeState.CHAINED, NodeState.APPENDING)) {
                tail.next = node;
                tail.nodeState = NodeState.CHAINED;
                return;
            }
        }
    }

    kotlin.Function1<V, kotlin.Unit> popSuccessCb() {
        return pop(successCbs);
    }

    kotlin.Function1<E, kotlin.Unit> popFailCb() {
        return pop(failCbs);
    }

    kotlin.Function0<kotlin.Unit> popAlwaysCb() {
        return pop(alwaysCbs);
    }

    private <T> T pop(ValueNode<T> head) {
        if (head == null) return null;
        for (ValueNode<T> poppable; (poppable = head.next) != null; ) {
            if (UnsafeAccess.compareAndSwapObject(head, nodeStateOffset, NodeState.CHAINED, NodeState.POPPING)) {
                if (UnsafeAccess.compareAndSwapObject(poppable, nodeStateOffset, NodeState.CHAINED, NodeState.POPPING)) {
                    head.next = poppable.next;
                    head.nodeState = NodeState.CHAINED;
                    return poppable.value;
                }
                head.nodeState = NodeState.CHAINED;
            }
        }
        return null;
    }


    private static class ValueNode<V> {
        final V value;
        volatile ValueNode<V> next = null;
        volatile NodeState nodeState = NodeState.CHAINED;

        ValueNode(V value) {
            if (value == null) {
                throw new IllegalArgumentException();
            }
            this.value = value;
        }
    }

}
