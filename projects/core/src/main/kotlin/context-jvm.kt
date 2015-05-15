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

package nl.mplatvoet.komponents.kovenant

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty

class ConcreteKovenant {
    val context: Context
        get() = mutableContext.get()

    private val mutableContext = AtomicReference(ThreadSafeContext())

    public fun configure(body: MutableContext.() -> Unit) {
        //a copy-on-write strategy is used, but in order to maintain the lazy loading mechanism
        //keeping track of what the developer actually altered is needed, otherwise
        //everything gets initialized during configuration
        val trackingContext = TrackingContext(mutableContext.get())
        trackingContext.body()

        do {
            val current = mutableContext.get()!!
            val newConfig = current.copy()
            trackingContext.applyChanged(newConfig)
        } while (!mutableContext.compareAndSet(current, newConfig))

    }

    fun createContext(body: MutableContext.() -> Unit): Context {
        val context = ThreadSafeContext()
        context.body()
        return context
    }

    public fun deferred<V, E>(context: Context = Kovenant.context): Deferred<V, E> = DeferredPromise(context)

    private class ThreadSafeContext() : MutableContext {

        private val callbackErrorDelegate = ThreadSafeLazyVar<(Exception) -> Unit> {
            { e: Exception -> throw e }
        }
        override var callbackError: (Exception) -> Unit by callbackErrorDelegate

        private val workerErrorDelegate = ThreadSafeLazyVar<(Exception) -> Unit> {
            { e: Exception -> throw e }
        }
        override var workerError: (Exception) -> Unit by workerErrorDelegate


        private val multipleCompletionDelegate = ThreadSafeLazyVar<(Any, Any) -> Unit> {
            { curVal: Any, newVal: Any -> throw IllegalStateException("Value[$curVal] is set, can't override with new value[$newVal]") }
        }
        override var multipleCompletion: (curVal: Any, newVal: Any) -> Unit by multipleCompletionDelegate


        private val callbackDispatcherDelegate: ThreadSafeLazyVar<Dispatcher> = ThreadSafeLazyVar {
            buildDispatcher {
                name = "kovenant-callback"
                numberOfThreads = 1
            }
        }
        private val workerDispatcherDelegate: ThreadSafeLazyVar<Dispatcher> = ThreadSafeLazyVar {
            buildDispatcher {
                name = "kovenant-worker"
            }
        }
        override var callbackDispatcher: Dispatcher by callbackDispatcherDelegate
        override var workerDispatcher: Dispatcher by workerDispatcherDelegate

        fun copy(): ThreadSafeContext {
            val copy = ThreadSafeContext()
            if (callbackErrorDelegate.initialized) copy.callbackError = callbackError
            if (workerErrorDelegate.initialized) copy.workerError = workerError
            if (callbackDispatcherDelegate.initialized) copy.callbackDispatcher = callbackDispatcher
            if (workerDispatcherDelegate.initialized) copy.workerDispatcher = workerDispatcher
            if (multipleCompletionDelegate.initialized) copy.multipleCompletion = multipleCompletion
            return copy
        }

        override val callbackContext: DispatcherContext = CallbackDispatcherContext()
        override val workerContext: DispatcherContext = WorkerDispatcherContext()

        private inner class CallbackDispatcherContext : DispatcherContext {
            override val dispatcher: Dispatcher
                get() = callbackDispatcher
            override val errorHandler: (Exception) -> Unit
                get() = callbackError

        }

        private inner class WorkerDispatcherContext : DispatcherContext {
            override val dispatcher: Dispatcher
                get() = workerDispatcher
            override val errorHandler: (Exception) -> Unit
                get() = workerError

        }
    }

    private class TrackingContext(private val currentConfig: Context) : MutableContext {
        private val callbackDispatcherDelegate = TrackChangesVar { currentConfig.callbackContext.dispatcher }
        private val workerDispatcherDelegate = TrackChangesVar { currentConfig.workerContext.dispatcher }


        override var callbackDispatcher: Dispatcher by callbackDispatcherDelegate
        override var workerDispatcher: Dispatcher by workerDispatcherDelegate

        private val callbackErrorDelegate = TrackChangesVar { currentConfig.callbackContext.errorHandler }
        override var callbackError: (Exception) -> Unit by callbackErrorDelegate

        private val workerErrorDelegate = TrackChangesVar { currentConfig.workerContext.errorHandler }
        override var workerError: (Exception) -> Unit by workerErrorDelegate

        private val multipleCompletionDelegate = TrackChangesVar { currentConfig.multipleCompletion }
        override var multipleCompletion: (curVal: Any, newVal: Any) -> Unit by multipleCompletionDelegate

        fun applyChanged(config: MutableContext) {
            if (callbackDispatcherDelegate.written) config.callbackDispatcher = callbackDispatcher
            if (workerDispatcherDelegate.written) config.workerDispatcher = workerDispatcher
            if (callbackErrorDelegate.written) config.callbackError = callbackError
            if (workerErrorDelegate.written) config.workerError = workerError
            if (multipleCompletionDelegate.written) config.multipleCompletion = multipleCompletion
        }

        override val callbackContext: DispatcherContext = CallbackDispatcherContext()
        override val workerContext: DispatcherContext = WorkerDispatcherContext()

        private inner class CallbackDispatcherContext : DispatcherContext {
            override val dispatcher: Dispatcher
                get() = callbackDispatcher
            override val errorHandler: (Exception) -> Unit
                get() = callbackError

        }

        private inner class WorkerDispatcherContext : DispatcherContext {
            override val dispatcher: Dispatcher
                get() = workerDispatcher
            override val errorHandler: (Exception) -> Unit
                get() = workerError

        }
    }


}


[suppress("UNCHECKED_CAST")]
private class ThreadSafeLazyVar<T>(initializer: () -> T) : ReadWriteProperty<Any?, T> {
    private volatile var threadCount: AtomicInteger? = AtomicInteger(0)
    private volatile var initializer: (() -> T)?
    private volatile var value: Any? = null

    init {
        this.initializer = initializer
    }

    public override fun get(thisRef: Any?, desc: PropertyMetadata): T {
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

    public override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        this.value = mask(value)
    }

    val initialized: Boolean get() = value != null
}

[suppress("UNCHECKED_CAST")]
private class TrackChangesVar<T>(private val source: () -> T) : ReadWriteProperty<Any?, T> {
    private volatile var value: Any? = null

    public override fun get(thisRef: Any?, desc: PropertyMetadata): T {
        val curVal = value
        return if (curVal != null) unmask(curVal) as T else source()
    }

    public override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        this.value = mask(value)
    }

    val written: Boolean get() = value != null
}

private val NULL_VALUE: Any = Any()
private fun mask(value: Any?): Any = value ?: NULL_VALUE
private fun unmask(value: Any?): Any? = if (value == NULL_VALUE) null else value
