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
@file:JvmName("KovenantFnContext")
package nl.komponents.kovenant.functional

import nl.komponents.kovenant.Context
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred


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
fun <V, E>Promise<V, E>.withContext(context: Context): Promise<V, E> {
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