package nl.mplatvoet.kotlin.komponents.promises;


import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by mark on 27/09/14.
 * Specialized Node class to create a non blocking link list. Created with the sole purpose of reducing the memory footprint and being optimized for performance.
 * Can't do this in Kotlin for now (or I don't know how) since we can't access fields directly as needed for the sun.misc.Unsafe
 *
 * There are no guards against circular references and there won't be for performance reasons.
 */
public class ValueNode<T> {
    private static final Unsafe UNSAFE = retrieveUnsafe();
    private static final long nextOffset;
    private static final long doneOffset;

    static {
        try {
            nextOffset = UNSAFE.objectFieldOffset(ValueNode.class.getDeclaredField("next"));
            doneOffset = UNSAFE.objectFieldOffset(ValueNode.class.getDeclaredField("done"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private final T _value;
    private volatile ValueNode<T> next = null;
    private volatile int done = 0;

    ValueNode(T value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        this._value = value;
    }

    T getValue() {
        return _value;
    }

    boolean isDone() {
        return done != 0;
    }


    ValueNode<T> getNext() {
        return next;
    }

    /**
     * Tries to append the given node to this node. If successful the give node is returned.
     * Otherwise the actual next node is returned.
     *
     * @param node the node to append. may not be null
     * @return the given node if successful, otherwise the actual next node. Never null.
     */

    private ValueNode<T> trySetNext(ValueNode<T> node) {
        if (next != null) {
            return next;
        }

        return UNSAFE.compareAndSwapObject(this, nextOffset, null, node) ? node : next;
    }

    /**
     * @return true if this operation changed the flag, false otherwise
     */
    boolean trySetDone() {
        return UNSAFE.compareAndSwapInt(this, doneOffset, 0, 1);
    }

    void append(ValueNode<T> node) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        ValueNode<T> tail = this;
        //noinspection StatementWithEmptyBody
        while ((tail = tail.trySetNext(node)) != node);
    }


    @SuppressWarnings("restriction")
    private static Unsafe retrieveUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }


}




