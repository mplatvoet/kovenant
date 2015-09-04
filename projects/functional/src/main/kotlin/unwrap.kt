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

import nl.komponents.kovenant.Context
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred


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
public fun <V : Any, E : Any> Promise<Promise<V, E>, E>.unwrap(context: Context = this.context): Promise<V, E> {
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
