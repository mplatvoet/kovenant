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

import nl.komponents.kovenant.Context
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.async
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.ReadOnlyProperty


private class LazyPromise<in R, T>(
        //Need to allow `null` context because we could easily
        //create this property before Kovenant gets configured.
        //that would lead to this property using another Context
        //than the rest of the program.
        private val context: Context?,
        initializer: () -> T) : ReadOnlyProperty<R, Promise<T, Exception>> {


    private volatile var initializer: (() -> T)?
    private volatile var value: Promise<T, Exception>? = null
    private volatile var threadCount: AtomicInteger? = AtomicInteger(0)

    init {
        this.initializer = initializer
    }

    override fun get(thisRef: R, desc: PropertyMetadata): Promise<T, Exception> {
        // Busy/Spin lock, expecting async to return quickly
        // Don't want to using blocking semantics since
        // it's not in the nature of Kovenant
        while (value == null) {
            val counter = threadCount
            if (counter != null) {
                val threadNumber = counter.incrementAndGet()
                if (threadNumber == 1) {
                    val fn = initializer!!
                    value = async(context ?: Kovenant.context) { fn() }
                    initializer = null // prevents memory leaking
                    threadCount = null //gc, you're up
                    break
                }
            }
            //Signal other threads are more important at the moment
            //Since another thread is initializing this property
            Thread.yield()
        }
        return value!!
    }
}

//TODO, M13 implementation
/*
private class LazyPromise<T : Any>(
        //Need to allow `null` context because we could easily
        //create this property before Kovenant gets configured.
        //that would lead to this property using another Context
        //than the rest of the program.
        private val context: Context?,
        initializer: () -> T) : Lazy<Promise<T, Exception>>() {



    private volatile var initializer: (() -> T)?
    private volatile var promise: Promise<T, Exception>? = null
    private volatile var threadCount: AtomicInteger? = AtomicInteger(0)

    init {
        this.initializer = initializer
    }

    override val value: Promise<T, Exception> get() = initOrGetPromise()
    override fun isInitialized(): Boolean = promise != null

    private fun initOrGetPromise(): Promise<T, Exception> {
        // Busy/Spin lock, expecting async to return quickly
        // Don't want to using blocking semantics since
        // it's not in the nature of Kovenant
        while (promise == null) {
            val counter = threadCount
            if (counter != null) {
                val threadNumber = counter.incrementAndGet()
                if (threadNumber == 1) {
                    val fn = initializer!!
                    promise = async(context ?: Kovenant.context) { fn() }
                    initializer = null // prevents memory leaking
                    threadCount = null //gc, you're up
                    break
                }
            }
            //Signal other threads are more important at the moment
            //Since another thread is initializing this property
            Thread.yield()
        }
        return promise!!
    }
}*/
