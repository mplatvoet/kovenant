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


@suppress("UNCHECKED_CAST")
public class ThreadSafeLazyVar<T>(initializer: () -> T) : ReadWriteProperty<Any?, T> {
    private volatile var threadCount: AtomicInteger? = AtomicInteger(0)
    private volatile var initializer: (() -> T)?
    private volatile var value: Any? = null

    init {
        this.initializer = initializer
    }

    public override fun get(thisRef: Any?, property: PropertyMetadata): T {
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
        return unmask(value) as T
    }

    public override fun set(thisRef: Any?, property: PropertyMetadata, value: T) {
        this.value = mask(value)
    }

    val initialized: Boolean get() = value != null
}

@suppress("UNCHECKED_CAST")
public class TrackChangesVar<T>(private val source: () -> T) : ReadWriteProperty<Any?, T> {
    private volatile var value: Any? = null

    public override fun get(thisRef: Any?, property: PropertyMetadata): T {
        val curVal = value
        return if (curVal != null) unmask(curVal) as T else source()
    }

    public override fun set(thisRef: Any?, property: PropertyMetadata, value: T) {
        this.value = mask(value)
    }

    val written: Boolean get() = value != null
}

private val NULL_VALUE: Any = Any()
private fun mask(value: Any?): Any = value ?: NULL_VALUE
private fun unmask(value: Any?): Any? = if (value == NULL_VALUE) null else value
