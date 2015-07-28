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

package nl.komponents.kovenant

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty

class ConcreteKovenant {
    private val contextRef: AtomicReference<Context> = AtomicReference(ThreadSafeContext())
    var context: Context
        get() {
            return contextRef.get()
        }
        set(value) {
            contextRef.set(value)
        }

    private val reconfigurableContext: ReconfigurableContext
        get() {
            val ctx = contextRef.get()
            if (ctx is ReconfigurableContext) {
                return ctx
            }
            throw ConfigurationException("Current context [$ctx] does not implement ReconfigurableContext and therefor can't be reconfigured.")
        }

    public fun context(body: MutableContext.() -> Unit): Context {
        //a copy-on-write strategy is used, but in order to maintain the lazy loading mechanism
        //keeping track of what the developer actually altered is needed, otherwise
        //everything gets initialized during configuration
        val trackingContext = TrackingContext(reconfigurableContext)
        trackingContext.body()

        do {
            val current = reconfigurableContext
            val newConfig = current.copy()
            trackingContext.applyChanged(newConfig)
        } while (!contextRef.compareAndSet(current, newConfig))
        return context
    }

    fun createContext(body: MutableContext.() -> Unit): Context {
        val context = ThreadSafeContext()
        context.body()
        return context
    }

    public fun deferred<V, E>(context: Context = Kovenant.context): Deferred<V, E> = DeferredPromise(context)

    private class ThreadSafeContext() : ReconfigurableContext {

        private val multipleCompletionDelegate = ThreadSafeLazyVar<(Any, Any) -> Unit> {
            { curVal: Any, newVal: Any -> throw IllegalStateException("Value[$curVal] is set, can't override with new value[$newVal]") }
        }
        override var multipleCompletion: (curVal: Any, newVal: Any) -> Unit by multipleCompletionDelegate

        val threadSafeCallbackContext = ThreadSafeMutableDispatcherContext() {
            buildDispatcher {
                name = "kovenant-callback"
                concurrentTasks = 1
            }
        }

        private val threadSafeWorkerContext = ThreadSafeMutableDispatcherContext() {
            buildDispatcher {
                name = "kovenant-worker"
            }
        }

        override val callbackContext: MutableDispatcherContext = object : MutableDispatcherContext by threadSafeCallbackContext {}
        override val workerContext: MutableDispatcherContext = object : MutableDispatcherContext by threadSafeWorkerContext {}

        override fun copy(): ReconfigurableContext {
            val copy = ThreadSafeContext()
            threadSafeCallbackContext copyTo copy.callbackContext
            threadSafeWorkerContext copyTo copy.workerContext
            if (multipleCompletionDelegate.initialized) copy.multipleCompletion = multipleCompletion
            return copy
        }


        private class ThreadSafeMutableDispatcherContext(factory: () -> Dispatcher) : MutableDispatcherContext {
            private val dispatcherDelegate: ThreadSafeLazyVar<Dispatcher> = ThreadSafeLazyVar(factory)
            private val errorHandlerDelegate = ThreadSafeLazyVar<(Exception) -> Unit> {
                { e: Exception -> throw e }
            }

            override var dispatcher: Dispatcher by dispatcherDelegate
            override var errorHandler: (Exception) -> Unit by errorHandlerDelegate

            fun copyTo(context: MutableDispatcherContext) {
                if (dispatcherDelegate.initialized) context.dispatcher = dispatcher
                if (errorHandlerDelegate.initialized) context.errorHandler = errorHandler
            }
        }
    }

    private class TrackingContext(private val currentContext: Context) : MutableContext {
        private val multipleCompletionDelegate = TrackChangesVar { currentContext.multipleCompletion }
        override var multipleCompletion: (curVal: Any, newVal: Any) -> Unit by multipleCompletionDelegate

        val trackingCallbackContext = TrackingMutableDispatcherContext(currentContext.callbackContext)
        override val callbackContext: MutableDispatcherContext = object : MutableDispatcherContext by trackingCallbackContext {}

        val trackingWorkerContext = TrackingMutableDispatcherContext(currentContext.workerContext)
        override val workerContext: MutableDispatcherContext = object : MutableDispatcherContext by trackingWorkerContext {}

        fun applyChanged(context: MutableContext) {
            trackingCallbackContext.applyChanged(context.callbackContext)
            trackingWorkerContext.applyChanged(context.workerContext)

            if (multipleCompletionDelegate.written) context.multipleCompletion = multipleCompletion
        }


        private class TrackingMutableDispatcherContext(private val source: DispatcherContext) : MutableDispatcherContext {
            private val dispatcherDelegate = TrackChangesVar { source.dispatcher }
            override var dispatcher: Dispatcher by dispatcherDelegate

            private val errorHandlerDelegate = TrackChangesVar { source.errorHandler }
            override var errorHandler: (Exception) -> Unit by errorHandlerDelegate

            fun applyChanged(context: MutableDispatcherContext) {
                if (dispatcherDelegate.written) context.dispatcher = dispatcher
                if (errorHandlerDelegate.written) context.errorHandler = errorHandler
            }
        }
    }


}


@suppress("UNCHECKED_CAST")
private class ThreadSafeLazyVar<T>(initializer: () -> T) : ReadWriteProperty<Any?, T> {
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
private class TrackChangesVar<T>(private val source: () -> T) : ReadWriteProperty<Any?, T> {
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
