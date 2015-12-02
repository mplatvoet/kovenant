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
@file:JvmName("KovenantUiContext")
package nl.komponents.kovenant.ui

import nl.komponents.kovenant.Context
import nl.komponents.kovenant.Dispatcher
import nl.komponents.kovenant.DispatcherContext


public object KovenantUi {
    private val concrete = ConcreteUiKovenant()
    public var uiContext: UiContext
        get() = concrete.uiContext
        set(value) {
            concrete.uiContext = value
        }

    public fun uiContext(body: MutableUiContext.() -> Unit): UiContext = concrete.uiContext(body)

    public fun createUiContext(body: MutableUiContext.() -> Unit): UiContext = concrete.createUiContext(body)

}

public fun UiContext.dispatcherContextFor(context: Context): DispatcherContext {
    return dispatcherContextBuilder(dispatcher, context)
}

public interface UiContext {
    val dispatcher: Dispatcher
    val dispatcherContextBuilder: (Dispatcher, Context) -> DispatcherContext
}

public interface MutableUiContext : UiContext {
    override var dispatcher: Dispatcher
    override var dispatcherContextBuilder: (Dispatcher, Context) -> DispatcherContext
}

public interface ReconfigurableUiContext : MutableUiContext {
    fun copy(): ReconfigurableUiContext
}

/**
 * Undocumented, may change in the future
 */
public class DelegatingDispatcherContext(private val base: DispatcherContext,
                                          override val dispatcher: Dispatcher) : DispatcherContext {
    override val errorHandler: (Exception) -> Unit
        get() = base.errorHandler
}



