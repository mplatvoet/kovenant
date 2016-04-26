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
@file:JvmName("KovenantUiApi")

package nl.komponents.kovenant.ui

import nl.komponents.kovenant.*

/**
 * Adds a success UI callback to this Promise
 *
 * Adds a success UI callback that gets executed on the provided UiDispatcherContext with the provided [ctx] of this
 * promise. Weakly store the [ctx] thus only gets executed if the [ctx] still exists when this promises resolves
 * successfully.
 *
 *
 * @param ctx contect to operate the callback on
 * @param uiContext the uitContext to operate on
 * @param alwaysSchedule whether the callback should always be scheduled, `false` by default.
 * @param callback the callback that gets executed on successful completion
 */
fun <C : Any, V, E> Promise<V, E>.successUi(ctx: C,
                                            uiContext: UiContext = KovenantUi.uiContext,
                                            alwaysSchedule: Boolean = false,
                                            callback: C.(V) -> Unit): Promise<V, E> {
    val dispatcherContext = uiContext.dispatcherContextFor(context)
    if (isDone() && directExecutionAllowed(alwaysSchedule, dispatcherContext.dispatcher)) {
        if (isSuccess()) {
            try {
                ctx.callback(get())
            } catch(e: Exception) {
                dispatcherContext.errorHandler(e)
            }
        }
    } else {
        registerWeakSuccess(callback, ctx, dispatcherContext)
    }
    return this
}


fun <C : Any, V, E> Promise<V, E>.failUi(ctx: C,
                                         uiContext: UiContext = KovenantUi.uiContext,
                                         alwaysSchedule: Boolean = false,
                                         body: C.(E) -> Unit): Promise<V, E> {
    val dispatcherContext = uiContext.dispatcherContextFor(context)
    if (isDone() && directExecutionAllowed(alwaysSchedule, dispatcherContext.dispatcher)) {
        if (isFailure()) {
            try {
                ctx.body(getError())
            } catch(e: Exception) {
                dispatcherContext.errorHandler(e)
            }
        }
    } else {
        registerWeakFail(body, ctx, dispatcherContext)
    }
    return this
}


fun <C : Any, V, E> Promise<V, E>.alwaysUi(ctx: C,
                                           uiContext: UiContext = KovenantUi.uiContext,
                                           alwaysSchedule: Boolean = false,
                                           body: C.() -> Unit): Promise<V, E> {
    val dispatcherContext = uiContext.dispatcherContextFor(context)
    if (isDone() && directExecutionAllowed(alwaysSchedule, dispatcherContext.dispatcher)) {

        try {
            ctx.body()
        } catch(e: Exception) {
            dispatcherContext.errorHandler(e)
        }
    } else {
        registerWeakAlways(body, ctx, dispatcherContext)
    }
    return this
}


@JvmOverloads fun <V> promiseOnUi(uiContext: UiContext = KovenantUi.uiContext,
                                  context: Context = Kovenant.context,
                                  alwaysSchedule: Boolean = false,
                                  body: () -> V): Promise<V, Exception> {
    if (directExecutionAllowed(alwaysSchedule, uiContext.dispatcher)) {
        return try {
            Promise.ofSuccess(context = context, value = body())
        } catch(e: Exception) {
            Promise.ofFail(context = context, value = e)
        }
    } else {
        val deferred = deferred<V, Exception>(context)
        uiContext.dispatcher.offer {
            try {
                val result = body()
                deferred.resolve(result)
            } catch(e: Exception) {
                deferred.reject(e)
            }
        }
        return deferred.promise
    }
}


infix fun <V, E> Promise<V, E>.successUi(body: (value: V) -> Unit): Promise<V, E> = successUi(alwaysSchedule = false, body = body)

@JvmOverloads fun <V, E> Promise<V, E>.successUi(uiContext: UiContext = KovenantUi.uiContext,
                                                 alwaysSchedule: Boolean,
                                                 body: (value: V) -> Unit): Promise<V, E> {

    val dispatcherContext = uiContext.dispatcherContextFor(context)
    if (isDone() && directExecutionAllowed(alwaysSchedule, dispatcherContext.dispatcher)) {
        if (isSuccess()) {
            try {
                body(get())
            } catch(e: Exception) {
                dispatcherContext.errorHandler(e)
            }
        }
    } else {
        success(dispatcherContext, body)
    }
    return this
}


infix fun <V, E> Promise<V, E>.failUi(body: (error: E) -> Unit): Promise<V, E> = failUi(alwaysSchedule = false, body = body)

@JvmOverloads fun <V, E> Promise<V, E>.failUi(uiContext: UiContext = KovenantUi.uiContext,
                                              alwaysSchedule: Boolean,
                                              body: (error: E) -> Unit): Promise<V, E> {
    val dispatcherContext = uiContext.dispatcherContextFor(context)
    if (isDone() && directExecutionAllowed(alwaysSchedule, dispatcherContext.dispatcher)) {
        if (isFailure()) {
            try {
                body(getError())
            } catch(e: Exception) {
                dispatcherContext.errorHandler(e)
            }
        }
    } else {
        fail(dispatcherContext, body)
    }
    return this
}


infix fun <V, E> Promise<V, E>.alwaysUi(body: () -> Unit): Promise<V, E> = alwaysUi(alwaysSchedule = false, body = body)

@JvmOverloads fun <V, E> Promise<V, E>.alwaysUi(uiContext: UiContext = KovenantUi.uiContext,
                                                alwaysSchedule: Boolean,
                                                body: () -> Unit): Promise<V, E> {
    val dispatcherContext = uiContext.dispatcherContextFor(context)
    if (isDone() && directExecutionAllowed(alwaysSchedule, dispatcherContext.dispatcher)) {
        try {
            body()
        } catch(e: Exception) {
            dispatcherContext.errorHandler(e)
        }
    } else {
        always(dispatcherContext, body)
    }
    return this
}

private fun directExecutionAllowed(alwaysSchedule: Boolean, dispatcher: Dispatcher): Boolean {
    return !alwaysSchedule && dispatcher is ProcessAwareDispatcher && dispatcher.ownsCurrentProcess()
}

