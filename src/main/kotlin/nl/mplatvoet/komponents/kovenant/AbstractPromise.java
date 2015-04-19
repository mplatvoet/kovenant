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

import java.util.concurrent.atomic.AtomicReference;

abstract class AbstractPromise<V, E> {
    private enum State {PENDING, MUTATING, SUCCESS, FAIL}

    private enum NodeState {CHAINED, POPPING, APPENDING}

    private static final long headOffset = UnsafeAccess.objectFieldOffset(AbstractPromise.class, "head");
    private static final long stateOffset = UnsafeAccess.objectFieldOffset(AbstractPromise.class, "state");


    private volatile CallbackContextNode<V, E> head = null;
    private volatile State state = State.PENDING;
    private volatile Object result = null;


    private CallbackContextNode<V, E> createHeadNode() {
        return new EmptyCallbackContextNode<V, E>();
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
        SuccessCallbackContextNode<V, E> node = new SuccessCallbackContextNode<V, E>(cb);
        addValueNode(node);
    }

    private void addValueNode(final CallbackContextNode<V, E> node) {
        //ensure there is a head
        while (head == null) {
            UnsafeAccess.compareAndSwapObject(this, headOffset, null, createHeadNode());
        }

        while (true) {
            CallbackContextNode<V, E> tail = head;
            while (tail.next != null) tail = tail.next;
            if (tail.nodeState.compareAndSet(NodeState.CHAINED, NodeState.APPENDING)) {
                if (tail.next == null) {
                    tail.next = node;
                    tail.nodeState.set(NodeState.CHAINED);
                    return;
                }
                tail.nodeState.set(NodeState.CHAINED);
            }
        }
    }

    void addFailCb(kotlin.Function1<E, kotlin.Unit> cb) {
        FailCallbackContextNode<V, E> node = new FailCallbackContextNode<V, E>(cb);
        addValueNode(node);
    }

    void addAlwaysCb(kotlin.Function0<kotlin.Unit> cb) {
        AlwaysCallbackContextNode<V, E> node = new AlwaysCallbackContextNode<V, E>(cb);
        addValueNode(node);
    }

    CallbackContext<V, E> popSuccessCb() {
        return pop(SuccessCallbackContextNode.class);
    }

    CallbackContext<V, E> popFailCb() {
        return pop(FailCallbackContextNode.class);
    }

    private <T extends CallbackContextNode<V, E>> CallbackContext<V, E> pop(Class<T> clazz) {
        if (head == null) return null;
        for (CallbackContextNode<V, E> poppable; (poppable = head.next) != null; ) {
            if (head.nodeState.compareAndSet(NodeState.CHAINED, NodeState.POPPING)) {
                if (poppable.nodeState.compareAndSet(NodeState.CHAINED, NodeState.POPPING)) {
                    head.next = poppable.next;
                    head.nodeState.set(NodeState.CHAINED);
                    if (poppable instanceof AlwaysCallbackContextNode || clazz.isAssignableFrom(poppable.getClass())) {
                        poppable.next = null;
                        return poppable;
                    }
                }
                head.nodeState.set(NodeState.CHAINED);
            }
        }
        return null;
    }

    interface CallbackContext<V, E> {
        void runSuccess(V value);

        void runFail(E value);
    }

    private abstract static class CallbackContextNode<V, E> implements CallbackContext<V, E> {
        volatile CallbackContextNode<V, E> next = null;
        AtomicReference<NodeState> nodeState = new AtomicReference<NodeState>(NodeState.CHAINED);
    }

    private static class EmptyCallbackContextNode<V, E> extends CallbackContextNode<V, E> {

        @Override
        public void runSuccess(V value) {
            //ignore
        }

        @Override
        public void runFail(E value) {
            //ignore
        }
    }

    protected static class AlwaysCallbackContextNode<V, E> extends CallbackContextNode<V, E> {
        private final kotlin.Function0<kotlin.Unit> fn;

        public AlwaysCallbackContextNode(Function0<Unit> fn) {
            if (fn == null) throw new IllegalArgumentException();
            this.fn = fn;
        }

        @Override
        public void runSuccess(V value) {
            fn.invoke();
        }

        @Override
        public void runFail(E value) {
            fn.invoke();
        }
    }

    protected static class SuccessCallbackContextNode<V, E> extends CallbackContextNode<V, E> {
        private final kotlin.Function1<V, kotlin.Unit> fn;

        public SuccessCallbackContextNode(Function1<V, Unit> fn) {
            if (fn == null) throw new IllegalArgumentException();
            this.fn = fn;
        }

        @Override
        public void runSuccess(V value) {
            fn.invoke(value);
        }

        @Override
        public void runFail(E value) {
            //ignore
        }
    }

    protected static class FailCallbackContextNode<V, E> extends CallbackContextNode<V, E> {
        private final kotlin.Function1<E, kotlin.Unit> fn;

        public FailCallbackContextNode(Function1<E, Unit> fn) {
            if (fn == null) throw new IllegalArgumentException();
            this.fn = fn;
        }

        @Override
        public void runSuccess(V value) {
            //ignore
        }

        @Override
        public void runFail(E value) {
            fn.invoke(value);
        }
    }

}
