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

package nl.mplatvoet.komponents.kovenant.android

import nl.mplatvoet.komponents.kovenant.*


public fun <V> promiseOnUi(context: Context = Kovenant.context, body: () -> V): Promise<V, Exception> {
    val deferred = deferred<V, Exception>(context)
    LooperExecutor.main submit PromiseUiRunnable(deferred, body)
    return deferred.promise
}


//TODO cache dispatcherContext?
public fun <V, E> Promise<V, E>.successUi(body: (value: V) -> Unit): Promise<V, E> {
    val dispatcherContext = DelegatingDispatcherContext(ctx.callbackContext, androidUiDispatcher())
    return success(dispatcherContext, body)
}

//TODO cache dispatcherContext?
public fun <V, E> Promise<V, E>.failUi(body: (error: E) -> Unit): Promise<V, E> {
    val dispatcherContext = DelegatingDispatcherContext(ctx.callbackContext, androidUiDispatcher())
    return fail(dispatcherContext, body)
}

//TODO cache dispatcherContext?
public fun <V, E> Promise<V, E>.alwaysUi(body: () -> Unit): Promise<V, E> {
    val dispatcherContext = DelegatingDispatcherContext(ctx.callbackContext, androidUiDispatcher())
    return always(dispatcherContext, body)
}


private class DelegatingDispatcherContext(private val base: DispatcherContext, override val dispatcher: Dispatcher) : DispatcherContext {
    override val errorHandler: (Exception) -> Unit
        get() = base.errorHandler
}

private class PromiseUiRunnable<V>(private val deferred: Deferred<V, Exception>,
                                   private val body: () -> V) : Runnable {
    override fun run() = try {
        val result = body()
        deferred.resolve(result)
    } catch(e: Exception) {
        deferred.reject(e)
    }
}


private val <V, E> Promise<V, E>.ctx: Context
    get() = when (this) {
        is ContextAware -> this.context
        else -> Kovenant.context
    }