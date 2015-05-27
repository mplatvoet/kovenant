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
     * must lead to the same instance of the Promise.
     */
    val promise: Promise<V, E>
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
 *
 * Any implementation must ensure that **all** callbacks are offered to their configured DispatcherContext in the order
 * they where added to this Promise.
 */
public trait Promise<V : Any, E : Any> {
    /**
     * The context that is associated with this Promise. By default all callbacks are executed on the callback
     * `DispatcherContext` of this context.
     *
     * Functions like `then`use this to base there returned promises on this to keep promises bound to a
     * desired context.
     */
    val context: Context

    /**
     * Adds a success callback to this Promise
     *
     * Adds a success callback that gets executed on the callbackContext of this promise standard context
     * when this Promise gets successfully resolved.
     *
     * Function added to allow this to be used in infix calls
     *
     * @param callback the callback that gets executed on successful completion
     */
    fun success(callback: (value: V) -> Unit): Promise<V, E> = success(context.callbackContext, callback)

    /**
     * Adds a fail callback to this Promise
     *
     * Adds a fail callback that gets executed on the callbackContext of this promise standard context
     * when this Promise gets rejected with an error or cancelled if this is a [CancelablePromise].
     *
     * Function added to allow this to be used in infix calls
     *
     * @param callback the callback to be executed on failure or cancellation
     */
    fun fail(callback: (error: E) -> Unit): Promise<V, E> = fail(context.callbackContext, callback)

    /**
     * Adds a always callback to this Promise
     *
     * Adds a always callback that gets executed on the callbackContext of this promise standard context
     * when this Promise gets resolved successfully, rejected with an error or cancelled if this is a
     * [CancelablePromise].
     *
     * Function added to allow this to be used in infix calls
     *
     * @param callback the callback to be executed on success, failure or cancellation
     */
    fun always(callback: () -> Unit): Promise<V, E> = always(context.callbackContext, callback)

    /**
     * Adds a success callback to this Promise
     *
     * Adds a success callback that gets executed on the provided callbackContext when this Promise gets successfully
     * resolved.
     *
     * @param callback the callback that gets executed on successful completion
     * @param context the DispatcherContext on which this callback is executed
     */
    fun success(context: DispatcherContext, callback: (value: V) -> Unit): Promise<V, E>


    /**
     * Adds a fail callback to this Promise
     *
     * Adds a fail callback that gets executed on the provided callbackContext when this Promise gets rejected with an
     * error or cancelled if this is a [CancelablePromise].
     *
     * @param callback the callback to be executed on failure or cancellation
     * @param context the DispatcherContext on which this callback is executed
     */
    fun fail(context: DispatcherContext, callback: (error: E) -> Unit): Promise<V, E>

    /**
     * Adds a always callback to this Promise
     *
     * Adds a always callback that gets executed on the provided callbackContext when this Promise gets resolved
     * successfully, rejected with an error or cancelled if this is a [CancelablePromise].
     *
     * Function added to allow this to be used in infix calls
     *
     * @param callback the callback to be executed on success, failure or cancellation
     * @param context the DispatcherContext on which this callback is executed
     */
    fun always(context: DispatcherContext, callback: () -> Unit): Promise<V, E>
}

/**
 * Creates a new [Deferred] instance.
 *
 * @param context the context on which the associated [Promise] operates on
 * @return newly created [Deferred]
 */
public fun deferred<V : Any, E : Any>(context: Context = Kovenant.context): Deferred<V, E> = Kovenant.deferred(context)


/**
 * Executes the given task on the work [DispatcherContext] of provided [Context] and returns a [Promise].
 * Any Ecxeption is considered a failure.
 *
 * @param body the task to be executed
 * @param context the context on which the task is executed and the [Promise] is tied to. `Kovenant.context` by default.
 * @return returns a [Promise] of inferred success type [V] and failure type [Exception]
 */
public fun async<V : Any>(context: Context = Kovenant.context,
                          body: () -> V): Promise<V, Exception> = concretePromise(context, body)

/**
 * Asynchronously bind the success value of a [Promise] and returns a new [Promise] with the transformed value.
 *
 * Transforms Promise A to Promise B. If Promise A resolves successful then [bind] is executed on the
 * work [DispatcherContext] of the default `Kovenant.context` and returns Promise B. If [bind] is successful,
 * meaning now Exception is thrown then Promise B resolves successful, failed otherwise.
 *
 * If Promise A fails with error E, Promise B will fail with error E too.
 *
 *
 * @param bind the transform function.
 */
public fun <V : Any, R : Any> Promise<V, Exception>.then(bind: (V) -> R): Promise<R, Exception> {
    return concretePromise(context, this, bind)
}

public fun <V : Any, R : Any> Promise<V, Exception>.then(context: Context, bind: (V) -> R): Promise<R, Exception> {
    return concretePromise(context, this, bind)
}

public inline fun <V : Any, R : Any> Promise<V, Exception>.thenUse(
        inlineOptions(InlineOption.ONLY_LOCAL_RETURN) bind: V.() -> R): Promise<R, Exception> = then { it.bind() }