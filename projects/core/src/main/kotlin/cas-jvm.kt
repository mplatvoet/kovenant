/*
 * Copyright (c) 2016 Mark Platvoet<mplatvoet@gmail.com>
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

package nl.komponents.kovenant.unsafe

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import kotlin.reflect.KClass

class UnsafeAtomicReferenceFieldUpdater<C : Any, V : Any>(targetClass: KClass<C>,
                                                          fieldName: String) : AtomicReferenceFieldUpdater<C, V>() {
    companion object {
        private val unsafe = getUnsafe()
    }

    private val offset: Long

    init {
        val field = targetClass.java.getDeclaredField(fieldName)
        offset = unsafe.objectFieldOffset(field)
    }

    override fun lazySet(target: C, newValue: V?) = unsafe.putOrderedObject(target, offset, newValue)
    override fun compareAndSet(target: C, expected: V?, update: V?): Boolean = unsafe.compareAndSwapObject(target, offset, expected, update)

    override fun set(target: C, newValue: V?) = unsafe.putObjectVolatile(target, offset, newValue);

    @Suppress("UNCHECKED_CAST")
    override fun get(target: C): V? = unsafe.getObjectVolatile(target, offset) as V

    //Equals AtomicReferenceFieldUpdater implementation on Java 8
    override fun weakCompareAndSet(target: C, expected: V?, update: V?): Boolean = compareAndSet(target, expected, update)
}

private val noUnsafeMarker = Any()
private @Volatile var unsafeInstance: Any? = null

fun hasUnsafe(): Boolean {
    if (unsafeInstance == null) {
        loadUnsafe()
    }
    println("has Unsafe: ${unsafeInstance != noUnsafeMarker}")
    return unsafeInstance != noUnsafeMarker
}

private fun loadUnsafe() {
    println("loading unsafe")
    try {
        val clazz = Class.forName("sun.misc.Unsafe")

        clazz.tryGetStaticField("theUnsafe") {
            unsafeInstance = it ?: noUnsafeMarker
            return
        }

        //KOV-78: Name on old dalvik implementations
        clazz.tryGetStaticField("THE_ONE") {
            unsafeInstance = it ?: noUnsafeMarker
            return
        }
    } catch(e: Exception) {
        //ignore
    }
    unsafeInstance = noUnsafeMarker
}

private inline fun Class<*>.tryGetStaticField(name: String, onFound: (Any?) -> Unit) {
    try {
        val field = getDeclaredField(name)
        field.isAccessible = true
        val fieldValue = field.get(null)
        onFound(fieldValue)
    } catch (e: Exception) {
        //ignore
    }
}

private fun getUnsafe(): sun.misc.Unsafe {
    if (unsafeInstance == null) {
        loadUnsafe()
    }
    if (unsafeInstance != noUnsafeMarker) {
        return unsafeInstance as sun.misc.Unsafe
    }

    throw RuntimeException("unsafe doesn't exist or is not accessible")
}