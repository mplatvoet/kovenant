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


/**
 * Created by mark on 27/09/14.
 * Specialized Node class to create a non blocking link list. Created with the sole purpose of reducing the memory footprint and being optimized for performance.
 * Can't do this in Kotlin for now (or I don't know how) since we can't access fields directly as needed for the sun.misc.Unsafe
 * <p/>
 * There are no guards against circular references and there won't be for performance reasons.
 */
public class ValueNode<V> {

    private static final long nextOffset = UnsafeAccess.objectFieldOffset(ValueNode.class, "next");
    private static final long markedOffset = UnsafeAccess.objectFieldOffset(ValueNode.class, "marked");


    private final V _value;
    private volatile ValueNode<V> next = null;
    private volatile int marked = 0;

    ValueNode(V value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        this._value = value;
    }

    V getValue() {
        return _value;
    }

    boolean isMarked() {
        return marked != 0;
    }


    ValueNode<V> getNext() {
        return next;
    }

    /**
     * Tries to append the given node to this node. If successful the give node is returned.
     * Otherwise the actual next node is returned.
     *
     * @param node the node to append. may not be null
     * @return the given node if successful, otherwise the actual next node. Never null.
     */

    private ValueNode<V> trySetNext(ValueNode<V> node) {
        if (next != null) {
            return next;
        }

        return UnsafeAccess.compareAndSwapObject(this, nextOffset, null, node) ? node : next;
    }

    /**
     * @return true if this operation changed the flag, false otherwise
     */
    boolean tryMark() {
        return UnsafeAccess.compareAndSwapInt(this, markedOffset, 0, 1);
    }

    void append(ValueNode<V> node) {
        assert node != null;

        ValueNode<V> tail = this;
        //noinspection StatementWithEmptyBody
        while ((tail = tail.trySetNext(node)) != node) ;
    }


}






