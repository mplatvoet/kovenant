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

package nl.komponents.kovenant.properties

import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.ReadWriteProperty


@Suppress("UNCHECKED_CAST")
public class ThreadSafeLazyVar<T>(initializer: () -> T) : ReadWriteProperty<Any?, T> {
    private @Volatile var threadCount: AtomicInteger? = AtomicInteger(0)
    private @Volatile var initializer: (() -> T)?
    private @Volatile var value: Any? = null

    init {
        this.initializer = initializer
    }

    public override operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
        //Busy /Spin lock, expect quick initialization
        while (value == null) {
            val counter = threadCount
            if (counter != null) {
                val threadNumber = counter.incrementAndGet()
                if (threadNumber == 1) {
                    val fn = initializer!!
                    value = mask(fn())
                    initializer = null //initialized, gc do your magic
                    threadCount = null //initialized, gc do your magic
                }
            }

            //Signal other threads are more important at the moment
            //Since another thread is initializing this property
            Thread.yield()
        }
        return unmask<T>(value)
    }

    public override operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: T) {
        this.value = mask(value)
    }

    val initialized: Boolean get() = value != null
}

@Suppress("UNCHECKED_CAST")
public class TrackChangesVar<T>(private val source: () -> T) : ReadWriteProperty<Any?, T> {
    private @Volatile var value: Any? = null

    public override operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): T {
        val curVal = value
        return if (curVal != null) unmask<T>(curVal) else source()
    }

    public override operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: T) {
        this.value = mask(value)
    }

    val written: Boolean get() = value != null
}

private val NULL_VALUE: Any = Any()

@Suppress("UNCHECKED_CAST")
internal fun <V>mask(value: V): V = value ?: NULL_VALUE as V

@Suppress("UNCHECKED_CAST")
internal fun <V>unmask(value: Any?): V = if (value == NULL_VALUE) null as V else value as V
