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
package nl.komponents.kovenant.ui

import nl.komponents.kovenant.*
import nl.komponents.kovenant.properties.ThreadSafeLazyVar
import nl.komponents.kovenant.properties.TrackChangesVar
import java.util.concurrent.atomic.AtomicReference

class ConcreteUiKovenant {
    private val uiContextRef: AtomicReference<UiContext> = AtomicReference(ThreadSafeUiContext())
    var uiContext: UiContext
        get() {
            return uiContextRef.get()
        }
        set(value) {
            uiContextRef.set(value)
        }

    private val reconfigurableUiContext: ReconfigurableUiContext
        get() {
            val ctx = uiContextRef.get()
            if (ctx is ReconfigurableUiContext) {
                return ctx
            }
            throw ConfigurationException("Current UiContext [$ctx] does not implement ReconfigurableUiContext and therefor can't be reconfigured.")
        }

    public fun uiContext(body: MutableUiContext.() -> Unit): UiContext {
        //a copy-on-write strategy is used, but in order to maintain the lazy loading mechanism
        //keeping track of what the developer actually altered is needed, otherwise
        //everything gets initialized during configuration
        val trackingUiContext = TrackingUiContext(reconfigurableUiContext)
        trackingUiContext.body()

        do {
            val current = reconfigurableUiContext
            val newConfig = current.copy()
            trackingUiContext.applyChanged(newConfig)
        } while (!uiContextRef.compareAndSet(current, newConfig))
        return uiContext
    }

    fun createUiContext(body: MutableUiContext.() -> Unit): UiContext {
        val UiContext = ThreadSafeUiContext()
        UiContext.body()
        return UiContext
    }


    private class ThreadSafeUiContext() : ReconfigurableUiContext {
        private val dispatcherDelegate: ThreadSafeLazyVar<Dispatcher> = ThreadSafeLazyVar {
            CurrentCallbackDispatcher()
        }

        private val dispatcherContextBuilderDelegate = ThreadSafeLazyVar {
            val cache = WeakReferenceCache<Dispatcher, WeakReferenceCache<Context, DispatcherContext>>() {
                dispatcher ->
                WeakReferenceCache<Context, DispatcherContext>() {
                    context ->
                    DelegatingDispatcherContext(context.callbackContext, dispatcher)
                }
            };

            { dispatcher: Dispatcher, context: Context -> cache[dispatcher][context] }
        }

        override var dispatcherContextBuilder: (Dispatcher, Context) -> DispatcherContext by dispatcherContextBuilderDelegate


        override var dispatcher: Dispatcher by dispatcherDelegate


        override fun copy(): ReconfigurableUiContext {
            val copy = ThreadSafeUiContext()
            if (dispatcherDelegate.initialized) copy.dispatcher = dispatcher
            if (dispatcherContextBuilderDelegate.initialized) copy.dispatcherContextBuilder = dispatcherContextBuilder
            return copy
        }


    }

    private class TrackingUiContext(private val currentUiContext: UiContext) : MutableUiContext {
        private val dispatcherContextBuilderDelegate = TrackChangesVar { currentUiContext.dispatcherContextBuilder }
        override var dispatcherContextBuilder: (Dispatcher, Context) -> DispatcherContext by dispatcherContextBuilderDelegate

        private val dispatcherDelegate = TrackChangesVar { currentUiContext.dispatcher }
        override var dispatcher: Dispatcher by dispatcherDelegate

        fun applyChanged(uiContext: MutableUiContext) {
            if (dispatcherDelegate.written) uiContext.dispatcher = dispatcher
            if (dispatcherContextBuilderDelegate.written) uiContext.dispatcherContextBuilder = dispatcherContextBuilder
        }
    }

    //Don't use delegation `by`, since the dispatcher might change/be reconfigured
    private class CurrentCallbackDispatcher() : Dispatcher {
        private val dispatcher: Dispatcher get() = Kovenant.context.callbackContext.dispatcher

        override fun offer(task: () -> Unit): Boolean = dispatcher offer task

        override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
            return dispatcher.stop(force, timeOutMs, block)
        }

        override fun tryCancel(task: () -> Unit): Boolean = dispatcher.tryCancel(task)

        override val terminated: Boolean
            get() = dispatcher.terminated
        override val stopped: Boolean
            get() = dispatcher.stopped
    }
}
