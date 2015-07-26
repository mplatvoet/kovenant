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

package nl.komponents.kovenant.functional

import nl.komponents.kovenant.*
import java.util.ArrayList

/**
 * Asynchronously bind the success value of a [Promise] and returns a new [Promise] with the transformed value.
 *
 * Transforms Promise A to Promise B. If Promise A resolves successful then [bind] is executed on the
 * work [DispatcherContext] of the default `Kovenant.context` and returns Promise B. If [bind] is successful,
 * meaning now Exception is thrown then Promise B resolves successful, failed otherwise.
 *
 * If Promise A fails with error E, Promise B will fail with error E too.
 *
 * @param bind the transform function.
 */
public fun <V : Any, R : Any> Promise<V, Exception>.map(bind: (V) -> R): Promise<R, Exception> = then(bind)

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
public fun <V : Any, R : Any> Promise<V, Exception>.map(context: Context, bind: (V) -> R): Promise<R, Exception> = then(context, bind)


/**
 * Asynchronously bind the success value of a [Promise] and returns a new [Promise] with the transformed value.
 *
 * Transforms Promise A to Promise B with a bind function that returns Promise B.
 * If Promise A resolves successful then [bind] is executed on the
 * work [DispatcherContext] of the default `Kovenant.context` and returns Promise B. If [bind] is successful,
 * meaning now Exception is thrown then Promise B resolves successful, failed otherwise.
 *
 * If Promise A fails with error E, Promise B will fail with error E too.
 *
 * @param bind the transform function.
 */
public fun <V : Any, R : Any> Promise<V, Exception>.flatMap(bind: (V) -> Promise<R, Exception>): Promise<R, Exception> = flatMap(context, bind)

/**
 * Asynchronously bind the success value of a [Promise] and returns a new [Promise] with the transformed value.
 *
 * Transforms Promise A to Promise B with a bind function that returns Promise B.
 * If Promise A resolves successful then [bind] is executed on the
 * work [DispatcherContext] of the default `Kovenant.context` and returns Promise B. If [bind] is successful,
 * meaning now Exception is thrown then Promise B resolves successful, failed otherwise.
 *
 * If Promise A fails with error E, Promise B will fail with error E too.
 *
 * @param context the on which the bind and returned Promise operate
 * @param bind the transform function.
 */
public fun <V : Any, R : Any> Promise<V, Exception>.flatMap(context: Context, bind: (V) -> Promise<R, Exception>): Promise<R, Exception> {
    if (isDone()) when {
        isSuccess() -> {
            val deferred = deferred<R, Exception>(context)
            asyncBind(bind, context, deferred, get())
            return deferred.promise
        }
        isFailure() -> return Promise.ofFail(getError(), context)
    }

    val deferred = deferred<R, Exception>(context)
    success {
        value ->
        asyncBind(bind, context, deferred, value)
    }
    fail {
        deferred reject it
    }

    return deferred.promise
}

private fun <R : Any, V : Any> asyncBind(bind: (V) -> Promise<R, Exception>,
                                         context: Context,
                                         deferred: Deferred<R, Exception>,
                                         value: V) {
    context.workerContext offer {
        try {
            val p = bind(value)
            p success { deferred resolve it }
            p fail { deferred reject it }
        } catch (e: Exception) {
            //just like map/then consider bind exception as rejection
            deferred reject e
        }
    }
}


/**
 * Undocumented API. Added as a public testable experimental feature. Implementation and signature might change.
 */
public fun <V, R> Sequence<V>.mapEach(context: Context = Kovenant.context, bind: (V) -> R): Promise<List<R>, Exception> {
    val deferred = deferred<List<R>, Exception>(context)
    context.workerContext offer {
        val promises = ArrayList<Promise<R, Exception>>()
        forEach {
            value ->
            promises add async(context) { bind(value) }
        }
        val masterPromise = all(promises)
        masterPromise success {
            deferred resolve it
        }
        masterPromise fail {
            deferred reject it
        }
    }

    return deferred.promise
}

/**
 * Unwraps any nested Promise.
 *
 * Unwraps any nested Promise. By default the returned `Promise` will operate on the same `Context` as its parent
 * `Promise`, no matter what the `Context` of the nested `Promise` is. If you want the resulting promise to operate on
 * a different `Context` you can provide one.
 *
 * Function tries to be as efficient as possible in cases where this or the nested `Promise` is already resolved. This
 * means that this function might or might not create a new `Promise`, it all depends on the current state.
 *
 * @param context the `Context` on which the returned `Promise` operates
 * @return the unwrapped Promise
 */
public fun <V, E> Promise<Promise<V, E>, E>.unwrap(context: Context = this.context): Promise<V, E> {
    if (isDone()) when {
        isSuccess() -> return get().withContext(context)
        isFailure() -> return Promise.ofFail(getError(), context)
    }

    val deferred = deferred<V, E>(context)
    success {
        nested ->
        nested success { deferred resolve it }
        nested fail { deferred reject it }
    } fail {
        deferred reject it
    }
    return deferred.promise
}


/**
 * Returns a `Promise` operating on the provided `Context`
 *
 * This function might return the same instance of the `Promise` or a new one depending whether the
 * `Context` of the `Promise` and the provided `Promise` match.
 *
 *
 * @param context The `Context` on which the returned promise should operate
 * @return the same `Promise` if the `Context` matches, a new promise otherwise with the provided context
 */
public fun <V, E>Promise<V, E>.withContext(context: Context): Promise<V, E> {
    // Already same context, just return self
    if (this.context == context) return this

    // avoid using deferred and callbacks if this promise
    // is already resolved
    if (isDone()) when {
        isSuccess() -> return Promise.ofSuccess(get(), context)
        isFailure() -> return Promise.ofFail(getError(), context)
    }

    //okay, the hard way
    val deferred = deferred<V, E>(context)
    success { deferred resolve it }
    fail { deferred reject it }
    return deferred.promise
}