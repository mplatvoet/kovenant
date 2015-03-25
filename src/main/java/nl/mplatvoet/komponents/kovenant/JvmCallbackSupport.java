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

package nl.mplatvoet.komponents.kovenant;

import kotlin.Function1;
import kotlin.Unit;

class JvmCallbackSupport<V, E> {
    private enum State {PENDING, MUTATING, SUCCESS, FAIL}
    private static final long successCbsOffset = UnsafeAccess.objectFieldOffset(JvmCallbackSupport.class, "successCbs");
    private static final long failCbsOffset = UnsafeAccess.objectFieldOffset(JvmCallbackSupport.class, "failCbs");
    private static final long alwaysCbsOffset = UnsafeAccess.objectFieldOffset(JvmCallbackSupport.class, "alwaysCbs");

    private static final long stateOffset = UnsafeAccess.objectFieldOffset(JvmCallbackSupport.class, "state");

    protected volatile ValueNode<kotlin.Function1<V, kotlin.Unit>> successCbs = null;
    protected volatile ValueNode<kotlin.Function1<E, kotlin.Unit>> failCbs = null;
    protected volatile ValueNode<kotlin.Function0<kotlin.Unit>> alwaysCbs = null;

    private volatile State state = State.PENDING;
    private volatile Object result = null;

    private boolean trySetRootSuccessCb(ValueNode<kotlin.Function1<V, kotlin.Unit>> node) {
        return successCbs == null && UnsafeAccess.compareAndSwapObject(this, successCbsOffset, null, node);
    }

    private boolean trySetRootFailCb(ValueNode<kotlin.Function1<E, kotlin.Unit>> node) {
        return failCbs == null && UnsafeAccess.compareAndSwapObject(this, failCbsOffset, null, node);
    }

    private boolean trySetRootAlwaysCb(ValueNode<kotlin.Function0<kotlin.Unit>> node) {
        return alwaysCbs == null && UnsafeAccess.compareAndSwapObject(this, alwaysCbsOffset, null, node);
    }


    boolean trySetSuccessResult(V result) {
        if (state != State.PENDING) return false;
        if(UnsafeAccess.compareAndSwapObject(this, stateOffset, State.PENDING, State.MUTATING)) {
            this.result = result;
            state = State.SUCCESS;
            return true;
        }
        return false;
    }

    boolean trySetFailResult(E result) {

        if (state != State.PENDING) return false;
        if(UnsafeAccess.compareAndSwapObject(this, stateOffset, State.PENDING, State.MUTATING)) {
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

        if (!trySetRootSuccessCb(node)) {
            successCbs.append(node);
        }
    }

    void addFailCb(kotlin.Function1<E, kotlin.Unit> cb) {
        ValueNode<kotlin.Function1<E, kotlin.Unit>> node = new ValueNode<Function1<E, Unit>>(cb);

        if (!trySetRootFailCb(node)) {
            failCbs.append(node);
        }
    }

    void addAlwaysCb(kotlin.Function0<kotlin.Unit> cb) {
        ValueNode<kotlin.Function0<kotlin.Unit>> node = new ValueNode<kotlin.Function0<Unit>>(cb);

        if (!trySetRootAlwaysCb(node)) {
            alwaysCbs.append(node);
        }
    }

}
