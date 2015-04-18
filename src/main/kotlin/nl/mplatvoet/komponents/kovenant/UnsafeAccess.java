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

import sun.misc.Unsafe;

import java.lang.reflect.Field;

final class UnsafeAccess {
    private static final Unsafe UNSAFE = retrieveUnsafe();

    private UnsafeAccess() {}

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

    static <T> long objectFieldOffset(Class<T> clazz, String fieldName) {
        try {
            return  UNSAFE.objectFieldOffset(clazz.getDeclaredField(fieldName));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    static boolean compareAndSwapObject(Object obj, long fieldOffset, Object expected, Object newValue) {
        return UNSAFE.compareAndSwapObject(obj, fieldOffset, expected, newValue);
    }

    static boolean compareAndSwapInt(Object obj, long fieldOffset, int expected, int newValue) {
        return UNSAFE.compareAndSwapInt(obj, fieldOffset, expected, newValue);
    }
}
