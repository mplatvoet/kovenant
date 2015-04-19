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

import kotlin.Function0;
import kotlin.Function1;
import kotlin.Unit;

abstract class AbstractPromise<V, E> {
    private enum State {PENDING, MUTATING, SUCCESS, FAIL}

    private enum NodeState {CHAINED, POPPING, APPENDING}

    private static final long headOffset = UnsafeAccess.objectFieldOffset(AbstractPromise.class, "head");
    private static final long stateOffset = UnsafeAccess.objectFieldOffset(AbstractPromise.class, "state");
    private static final long nodeStateOffset = UnsafeAccess.objectFieldOffset(ValueNode.class, "nodeState");


    private volatile ValueNode<V, E> head = null;
    private volatile State state = State.PENDING;
    private volatile Object result = null;


    private ValueNode<V, E> createHeadNode() {
        return new RootNode<V, E>();
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
        ValueNode<V, E> node = new SuccessNode<V, E>(cb);
        addValueNode(node);
    }

    private void addValueNode(final ValueNode<V, E> node) {
        //ensure there is a head
        while (head == null) {
            UnsafeAccess.compareAndSwapObject(this, headOffset, null, createHeadNode());
        }

        while (true) {
            ValueNode<V, E> tail = head;
            while (tail.next != null) tail = tail.next;
            if (UnsafeAccess.compareAndSwapObject(tail, nodeStateOffset, NodeState.CHAINED, NodeState.APPENDING)) {
                tail.next = node;
                tail.nodeState = NodeState.CHAINED;
                return;
            }
        }
    }

    void addFailCb(kotlin.Function1<E, kotlin.Unit> cb) {
        ValueNode<V, E> node = new FailNode<V, E>(cb);
        addValueNode(node);
    }

    void addAlwaysCb(kotlin.Function0<kotlin.Unit> cb) {
        ValueNode<V, E> node = new AlwaysNode<V, E>(cb);
        addValueNode(node);
    }

    ValueNode<V, E> popSuccessCb() {
        return pop(SuccessNode.class);
    }

    ValueNode<V, E> popFailCb() {
        return pop(FailNode.class);
    }

    private <T extends ValueNode<V, E>> ValueNode<V, E> pop(Class<T> clazz) {
        if (head == null) return null;
        for (ValueNode<V, E> poppable; (poppable = head.next) != null; ) {
            if (UnsafeAccess.compareAndSwapObject(head, nodeStateOffset, NodeState.CHAINED, NodeState.POPPING)) {
                if (UnsafeAccess.compareAndSwapObject(poppable, nodeStateOffset, NodeState.CHAINED, NodeState.POPPING)) {
                    head.next = poppable.next;
                    head.nodeState = NodeState.CHAINED;
                    if (poppable instanceof AlwaysNode || clazz.isAssignableFrom(poppable.getClass())) {
                        return poppable;
                    }
                }
                head.nodeState = NodeState.CHAINED;
            }
        }
        return null;
    }


    protected abstract static class ValueNode<V, E> {
        volatile ValueNode<V, E> next = null;
        volatile NodeState nodeState = NodeState.CHAINED;

        abstract void runSuccess(V value);

        abstract void runFail(E value);
    }

    private static class RootNode<V, E> extends ValueNode<V, E> {

        @Override
        void runSuccess(V value) {
            //ignore
        }

        @Override
        void runFail(E value) {
            //ignore
        }
    }

    protected static class AlwaysNode<V, E> extends ValueNode<V, E> {
        private final kotlin.Function0<kotlin.Unit> fn;

        public AlwaysNode(Function0<Unit> fn) {
            if (fn == null) throw new IllegalArgumentException();
            this.fn = fn;
        }

        @Override
        void runSuccess(V value) {
            fn.invoke();
        }

        @Override
        void runFail(E value) {
            fn.invoke();
        }
    }

    protected static class SuccessNode<V, E> extends ValueNode<V, E> {
        private final kotlin.Function1<V, kotlin.Unit> fn;

        public SuccessNode(Function1<V, Unit> fn) {
            if (fn == null) throw new IllegalArgumentException();
            this.fn = fn;
        }

        @Override
        void runSuccess(V value) {
            fn.invoke(value);
        }

        @Override
        void runFail(E value) {
            //ignore
        }
    }

    protected static class FailNode<V, E> extends ValueNode<V, E> {
        private final kotlin.Function1<E, kotlin.Unit> fn;

        public FailNode(Function1<E, Unit> fn) {
            if (fn == null) throw new IllegalArgumentException();
            this.fn = fn;
        }

        @Override
        void runSuccess(V value) {
            //ignore
        }

        @Override
        void runFail(E value) {
            fn.invoke(value);
        }
    }

}
