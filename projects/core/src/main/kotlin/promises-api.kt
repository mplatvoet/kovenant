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
public trait Deferred<V : Any, E : Any> {
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
 * Mark a class to be cancelable
 *
 * What cancelling exactly means is up to the implementor.
 * But the intention is stopping.
 */
public trait CancelablePromise<V : Any, E : Any> : Promise<V, E> {
    fun cancel(error: E): Boolean
}

/**
 * A construct for receiving a notification of an asynchronous job
 *
 * A Promise can either resolve in [success] or get rejected and [fail].
 * Either way, it will [always] let you know.
 */
public trait Promise<V : Any, E : Any> {
    fun success(callback: (value: V) -> Unit): Promise<V, E> = success(null, callback)
    fun fail(callback: (error: E) -> Unit): Promise<V, E> = fail(null, callback)
    fun always(callback: () -> Unit): Promise<V, E> = always(null, callback)

    fun success(context: DispatcherContext?, callback: (value: V) -> Unit): Promise<V, E>
    fun fail(context: DispatcherContext?, callback: (error: E) -> Unit): Promise<V, E>
    fun always(context: DispatcherContext?, callback: () -> Unit): Promise<V, E>
}


public fun deferred<V : Any, E : Any>(context: Context = Kovenant.context): Deferred<V, E> = Kovenant.deferred(context)


public fun async<V : Any>(context: Context = Kovenant.context,
                          body: () -> V): Promise<V, Exception> = concretePromise(context, body)

public fun <V : Any, R : Any> Promise<V, Exception>.then(bind: (V) -> R): Promise<R, Exception> {
    val context = when (this) {
        is ContextAware -> this.context
        else -> Kovenant.context
    }

    return concretePromise(context, this, bind)
}

public inline fun <V : Any, R : Any> Promise<V, Exception>.thenUse(
        inlineOptions(InlineOption.ONLY_LOCAL_RETURN) bind: V.() -> R): Promise<R, Exception> = then { it.bind() }