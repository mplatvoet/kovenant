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

import java.util.concurrent.atomic.AtomicReference

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
    }

    private class TrackingContext(private val currentConfig: Context) : MutableContext {
        private val callbackDispatcherDelegate = TrackChangesVar { currentConfig.callbackDispatcher }
        private val workerDispatcherDelegate = TrackChangesVar { currentConfig.workerDispatcher }


        override var callbackDispatcher: Dispatcher by callbackDispatcherDelegate
        override var workerDispatcher: Dispatcher by workerDispatcherDelegate

        private val callbackErrorDelegate = TrackChangesVar { currentConfig.callbackError }
        override var callbackError: (Exception) -> Unit by callbackErrorDelegate

        private val workerErrorDelegate = TrackChangesVar { currentConfig.workerError }
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
    }


}
