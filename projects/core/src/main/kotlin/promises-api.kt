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
@file:JvmName("KovenantApi")
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
public interface Deferred<V, E> {
    /**
     * Resolves this deferred with the provided value
     *
     * It's up to the implementation what happens when a Deferred gets
     * resolved multiple times. It may simply be ignored or throw
     * an Exception.
     *
     * @param [value] the value to resolve this deferred with
     */
    public infix fun resolve(value: V)

    /**
     * Rejects this deferred with the provided error
     *
     * It's up to the implementation what happens when a Deferred gets
     * rejected multiple times. It may simply be ignored or throw
     * an Exception.
     *
     * @param [error] the value to reject this deferred with
     */
    public infix fun reject(error: E)

    /**
     * Holds the accompanied [Promise]
     *
     * The accompanied [Promise] for this deferred. Multiple invocations
     * must lead to the same instance of the Promise.
     */
    public val promise: Promise<V, E>
}

/**
 * Resolves a Deferred of type <Unit, E> with Unit.
 * This makes it just a bit more natural looking
 */
public fun <E> Deferred<Unit, E>.resolve() = resolve(Unit)

/**
 * Rejects a Deferred of type <V, Unit> with Unit.
 * This makes it just a bit more natural looking
 */
public fun <V> Deferred<V, Unit>.reject() = reject(Unit)


/**
 * Mark a class to be cancelable
 *
 * What cancelling exactly means is up to the implementor.
 * But the intention is stopping.
 */
public interface CancelablePromise<V, E> : Promise<V, E> {
    public fun cancel(error: E): Boolean
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
public interface Promise<out V, out E> {
    companion object {
        /**
         * Takes any value `V` and wraps it as a successfully resolved promise.
         *
         * @param context the Context associated with the promise
         * @param value the value to wrap into a Promise<V, Exception>
         */
        public fun <V> of(value: V, context: Context = Kovenant.context): Promise<V, Exception> {
            return concreteSuccessfulPromise(context, value)
        }

        /**
         * Takes any value `V` and wraps it as a successfully resolved promise.
         *
         * @param context the Context associated with the promise
         * @param value the value to wrap into a Promise<V, E>
         */
        public fun <V, E> ofSuccess(value: V, context: Context = Kovenant.context): Promise<V, E> {
            return concreteSuccessfulPromise(context, value)
        }

        /**
         * Takes any value `E` and wraps it as a failed resolved promise.
         *
         * @param context the Context associated with the promise
         * @param value the value to wrap into a failed Promise<V, E>
         */
        public fun <V, E> ofFail(value: E, context: Context = Kovenant.context): Promise<V, E> {
            return concreteFailedPromise(context, value)
        }

    }

    /**
     * The context that is associated with this Promise. By default all callbacks are executed on the callback
     * `DispatcherContext` of this context.
     *
     * Functions like `then`use this to base there returned promises on this to keep promises bound to a
     * desired context.
     */
    public val context: Context

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
    public infix fun success(callback: (value: V) -> Unit): Promise<V, E> = success(context.callbackContext, callback)

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
    public infix fun fail(callback: (error: E) -> Unit): Promise<V, E> = fail(context.callbackContext, callback)

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
    public infix fun always(callback: () -> Unit): Promise<V, E> = always(context.callbackContext, callback)

    /**
     * Adds a success callback to this Promise
     *
     * Adds a success callback that gets executed on the provided callbackContext when this Promise gets successfully
     * resolved.
     *
     * @param callback the callback that gets executed on successful completion
     * @param context the DispatcherContext on which this callback is executed
     */
    public fun success(context: DispatcherContext, callback: (value: V) -> Unit): Promise<V, E>


    /**
     * Adds a fail callback to this Promise
     *
     * Adds a fail callback that gets executed on the provided callbackContext when this Promise gets rejected with an
     * error or cancelled if this is a [CancelablePromise].
     *
     * @param callback the callback to be executed on failure or cancellation
     * @param context the DispatcherContext on which this callback is executed
     */
    public fun fail(context: DispatcherContext, callback: (error: E) -> Unit): Promise<V, E>

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
    public fun always(context: DispatcherContext, callback: () -> Unit): Promise<V, E>

    /**
     * Blocks until this promises is done and either immediate returning the success result or throwing an `Exception`
     *
     * Blocks until this promises is done. When this promise is successful this will return success value `V`.
     * When this promise failed this will throw an exception. If the type of `E` is an Exception this will be thrown
     * otherwise a `FailedException` will be thrown with the error value wrapped.
     *
     * @return returns the success value when done
     */
    @Throws(Exception::class)
    public fun get(): V

    /**
     * Blocks until this promises is done and either immediate returning the failure result or throwing a `FailedException`
     *
     * Blocks until this promises is done. When this promise has failed this will return the failure value `E`.
     * When this promise is successful this will throw a `FailedException`.
     *
     * @return returns the fail value when done
     */
    @Throws(FailedException::class)
    public fun getError(): E


    /**
     * Returns true if this promise is either resolved successfully or has failed
     *
     * @return true if this promise is either resolved successfully or has failed, false otherwise
     */
    public fun isDone(): Boolean

    /**
     * Returns true if this promise is resolved a failed
     *
     * @return true if this promise is resolved a failed, false otherwise
     */
    public fun isFailure(): Boolean

    /**
     * Returns true if this promise is resolved successfully
     *
     * @return true if this promise is resolved successfully, false otherwise
     */
    public fun isSuccess(): Boolean
}


/**
 * Creates a new [Deferred] instance.
 *
 * @param context the context on which the associated [Promise] operates on
 * @return newly created [Deferred]
 */
public fun <V, E> deferred(context: Context = Kovenant.context): Deferred<V, E> = Kovenant.deferred(context)


/**
 * Executes the given task on the work [DispatcherContext] of provided [Context] and returns a [Promise].
 * Any Ecxeption is considered a failure.
 *
 * @param body the task to be executed
 * @param context the context on which the task is executed and the [Promise] is tied to. `Kovenant.context` by default.
 * @return returns a [Promise] of inferred success type [V] and failure type [Exception]
 */
@Deprecated("async is a keyword, favor task instead", ReplaceWith("task(context, body)"))
public fun <V> async(context: Context = Kovenant.context,
                    body: () -> V): Promise<V, Exception> = task(context, body)

/**
 * Executes the given task on the work [DispatcherContext] of provided [Context] and returns a [Promise].
 * Any Exception is considered a failure.
 *
 * @param body the task to be executed
 * @param context the context on which the task is executed and the [Promise] is tied to. `Kovenant.context` by default.
 * @return returns a [Promise] of inferred success type [V] and failure type [Exception]
 */
@JvmOverloads
public fun <V> task(context: Context = Kovenant.context,
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
public infix fun <V, R> Promise<V, Exception>.then(bind: (V) -> R): Promise<R, Exception> {
    return concretePromise(context, this, bind)
}

/**
 * Asynchronously bind the success value of a [Promise] and returns a new [Promise] with the transformed value.
 *
 * Transforms Promise A to Promise B. If Promise A resolves successful then [bind] is executed on the
 * work [DispatcherContext] of the default `Kovenant.context` and returns Promise B. If [bind] is successful,
 * meaning now Exception is thrown then Promise B resolves successful, failed otherwise.
 *
 * If Promise A fails with error E, Promise B will fail with error E too.
 *
 * @param context the on which the bind and returned Promise operate
 * @param bind the transform function.
 */
public fun <V, R> Promise<V, Exception>.then(context: Context, bind: (V) -> R): Promise<R, Exception> {
    return concretePromise(context, this, bind)
}

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
public infix inline fun <V, R> Promise<V, Exception>.thenUse(
        crossinline bind: V.() -> R): Promise<R, Exception> = then { it.bind() }


/**
 * Transforms any `Promise<V, E>` into a Promise<Unit, Unit>.
 *
 * The purpose is to hide any result from the consumer but still give the ability to know when something is ready.
 *
 * @return returns the Promise<Unit, Unit> with both value and error hidden
 */
public fun <V, E> Promise<V, E>.toVoid(context: Context = this.context): Promise<Unit, Unit> {
    if (isDone()) {
        if (isSuccess()) return Promise.ofSuccess(Unit, context)
        if (isFailure()) return Promise.ofFail(Unit, context)
    }

    val deferred = deferred<Unit, Unit>(context)
    success { deferred.resolve() }
    fail { deferred.reject() }
    return deferred.promise
}

/**
 * Transforms any `Promise<V, E>` into a Promise<V, Unit>.
 *
 * The purpose is to hide any result from the consumer but still give the ability to know when something is ready.
 * Hides the error only
 *
 * @return returns the Promise<V, Unit> with the error hidden
 */
public fun <V, E> Promise<V, E>.toFailVoid(context: Context = this.context): Promise<V, Unit> {
    if (isDone()) {
        if (isSuccess()) return Promise.ofSuccess<V, Unit>(get(), context)
        if (isFailure()) return Promise.ofFail(Unit, context)
    }

    val deferred = deferred<V, Unit>(context)
    success { deferred.resolve(it) }
    fail { deferred.reject() }
    return deferred.promise
}

/**
 * Transforms any `Promise<V, E>` into a Promise<Unit, E>.
 *
 * The purpose is to hide any result from the consumer but still give the ability to know when something is ready.
 * Hides the value only.
 *
 * @return returns the Promise<Unit, V> with the value hidden
 */
public fun <V, E> Promise<V, E>.toSuccessVoid(context: Context = this.context): Promise<Unit, E> {
    if (isDone()) {
        if (isSuccess()) return Promise.ofSuccess<Unit, E>(Unit, context)
        if (isFailure()) return Promise.ofFail(getError(), context)
    }

    val deferred = deferred<Unit, E>(context)
    success { deferred.resolve() }
    fail { deferred.reject(it) }
    return deferred.promise
}