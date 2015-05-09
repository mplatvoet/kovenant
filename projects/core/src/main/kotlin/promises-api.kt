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


/**
 * Deferred is the private part of the [Promise]
 *
 * A Deferred object let's you control a an accompanied [Promise] object
 * by either [resolve] or [reject] it. Any implementation must make sure
 * that a Deferred object can only be completed once as either resolved
 * or rejected.
 *
 * It's up to the implementation what happens when a Deferred gets
 * resolved or rejected multiple times. It may simply be ignored or throw
 * an Exception.
 */
public trait Deferred<V, E> {
    /**
     * Resolves this deferred with the provided value
     *
     * It's up to the implementation what happens when a Deferred gets
     * resolved multiple times. It may simply be ignored or throw
     * an Exception.
     *
     * @param [value] the value to resolve this deferred with
     */
    fun resolve(value: V)

    /**
     * Rejects this deferred with the provided error
     *
     * It's up to the implementation what happens when a Deferred gets
     * rejected multiple times. It may simply be ignored or throw
     * an Exception.
     *
     * @param [error] the value to reject this deferred with
     */
    fun reject(error: E)

    /**
     * Holds the accompanied [Promise]
     *
     * The accompanied [Promise] for this deferred. Multiple invocations
     * must lead to the some instance of the Promise.
     */
    val promise: Promise<V, E>
}

/**
 * Mark an class to be aware of the context
 *
 * Used by [then] to obtain the current context from a [Promise]
 *
 */
public trait ContextAware {
    val context: Context
}

/**
 * A construct for receiving a notification of an asynchronous job
 *
 * A Promise can either resolve in [success] or get rejected and [fail].
 * Either way, it will [always] let you know.
 */
public trait Promise<V, E> {
    fun success(callback: (value: V) -> Unit): Promise<V, E>
    fun fail(callback: (error: E) -> Unit): Promise<V, E>
    fun always(callback: () -> Unit): Promise<V, E>
}


public fun deferred<V, E>(context: Context = Kovenant.context): Deferred<V, E> = Kovenant.deferred(context)

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
    val context = when (this) {
        is ContextAware -> this.context
        else -> Kovenant.context
    }

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

public inline fun <V, R> Promise<V, Exception>.thenUse(
        inlineOptions(InlineOption.ONLY_LOCAL_RETURN) bind: V.() -> R
): Promise<R, Exception> = then { it.bind() }