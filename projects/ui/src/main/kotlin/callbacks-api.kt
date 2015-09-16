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


public fun <V : Any> promiseOnUi(uiContext: UiContext = KovenantUi.uiContext,
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
        uiContext.dispatcher offer {
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


public fun <V : Any, E : Any> Promise<V, E>.successUi(body: (value: V) -> Unit): Promise<V, E> = successUi(alwaysSchedule = false, body = body)

public fun <V : Any, E : Any> Promise<V, E>.successUi(uiContext: UiContext = KovenantUi.uiContext,
                                          alwaysSchedule: Boolean,
                                          body: (value: V) -> Unit): Promise<V, E> {

    val dispatcherContext = uiContext dispatcherContextFor context
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


public fun <V : Any, E : Any> Promise<V, E>.failUi(body: (error: E) -> Unit): Promise<V, E> = failUi(alwaysSchedule = false, body = body)

public fun <V : Any, E : Any> Promise<V, E>.failUi(uiContext: UiContext = KovenantUi.uiContext,
                                       alwaysSchedule: Boolean,
                                       body: (error: E) -> Unit): Promise<V, E> {
    val dispatcherContext = uiContext dispatcherContextFor context
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


public fun <V : Any, E : Any> Promise<V, E>.alwaysUi(body: () -> Unit): Promise<V, E> = alwaysUi(alwaysSchedule = false, body = body)

public fun <V : Any, E : Any> Promise<V, E>.alwaysUi(uiContext: UiContext = KovenantUi.uiContext,
                                         alwaysSchedule: Boolean,
                                         body: () -> Unit): Promise<V, E> {
    val dispatcherContext = uiContext dispatcherContextFor context
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