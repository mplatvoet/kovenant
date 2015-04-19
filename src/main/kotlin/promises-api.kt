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

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.RejectedExecutionException
import kotlin.InlineOption.ONLY_LOCAL_RETURN

public trait Deferred<V, E> {
    fun resolve(value: V)
    fun reject(error: E)
    val promise: Promise<V, E>
}


public trait Promise<V, E> {
    val context : Context
    fun success(callback: (value: V) -> Unit): Promise<V, E>
    fun fail(callback: (error: E) -> Unit): Promise<V, E>
    fun always(callback: () -> Unit): Promise<V, E>
}


public fun deferred<V, E>(context: Context = Kovenant.context) : Deferred<V, E> = Kovenant.deferred(context)

private fun Context.tryDispatch(fn: () -> Unit) = callbackDispatcher.offer (fn, callbackError)

private fun Context.tryWork(fn: () -> Unit) = workerDispatcher.offer (fn, workerError)

private fun Dispatcher.offer(fn: () -> Unit, errorFn: (Exception) -> Unit) {
    try {
        this.offer(fn)
    } catch (e: Exception) {
        errorFn(e)
    }
}

public fun async<V>(context: Context = Kovenant.context, body: () -> V): Promise<V, Exception> {
    val deferred = deferred<V, Exception>(context)
    context.tryWork {
        try {
            val result = body()
            deferred.resolve(result)
        } catch(e: Exception) {
            deferred.reject(e)
        }
    }
    return deferred.promise
}

public fun <V, R> Promise<V, Exception>.then(bind: (V) -> R): Promise<R, Exception> {
    val deferred = deferred<R, Exception>(context)
    success {
        context.tryWork {
            try {
                val result = bind(it)
                deferred.resolve(result)
            } catch(e: Exception) {
                deferred.reject(e)
            }
        }
    }
    fail {
        deferred.reject(it)
    }
    return deferred.promise
}