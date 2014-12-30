package nl.mplatvoet.kotlin.komponents.promises;

import kotlin.Function1;
import kotlin.Unit;

class JvmCallbackSupport<V, E> {
    private static final long successCbsOffset = UnsafeAccess.objectFieldOffset(JvmCallbackSupport.class, "successCbs");
    private static final long failCbsOffset = UnsafeAccess.objectFieldOffset(JvmCallbackSupport.class, "failCbs");
    private static final long alwaysCbsOffset = UnsafeAccess.objectFieldOffset(JvmCallbackSupport.class, "alwaysCbs");

    private static final long resultOffset = UnsafeAccess.objectFieldOffset(JvmCallbackSupport.class, "result");

    protected volatile ValueNode<kotlin.Function1<V, kotlin.Unit>> successCbs = null;
    protected volatile ValueNode<kotlin.Function1<E, kotlin.Unit>> failCbs = null;
    protected volatile ValueNode<kotlin.Function0<kotlin.Unit>> alwaysCbs = null;

    protected volatile Result<V, E> result = null;

    private boolean trySetRootSuccessCb(ValueNode<kotlin.Function1<V, kotlin.Unit>> node) {
        return successCbs == null && UnsafeAccess.compareAndSwapObject(this, successCbsOffset, null, node);
    }

    private boolean trySetRootFailCb(ValueNode<kotlin.Function1<E, kotlin.Unit>> node) {
        return failCbs == null && UnsafeAccess.compareAndSwapObject(this, failCbsOffset, null, node);
    }

    private boolean trySetRootAlwaysCb(ValueNode<kotlin.Function0<kotlin.Unit>> node) {
        return alwaysCbs == null && UnsafeAccess.compareAndSwapObject(this, alwaysCbsOffset, null, node);
    }



    boolean trySetResult(Result<V, E> result) {
        return UnsafeAccess.compareAndSwapObject(this, resultOffset, null, result);
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
