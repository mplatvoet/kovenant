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


public fun <V : Any, R : Any> Promise<V, Exception>.map(bind: (V) -> R): Promise<R, Exception> = then(bind)


public fun <V : Any, R : Any> Promise<V, Exception>.flatMap(bind: (V) -> Promise<R, Exception>): Promise<R, Exception> {
    val deferred = deferred<R, Exception>(context)
    success {
        value ->
        context.workerContext.offer {
            val p = bind(value)
            p success {
                deferred resolve it
            }
            p fail {
                deferred reject it
            }
        }
    }
    fail {
        deferred reject it
    }

    return deferred.promise
}

fun <V, R> Sequence<V>.aforEach(context: Context = Kovenant.context, bind: (V) -> R): Promise<List<R>, Exception> {
    val deferred = deferred<List<R>, Exception>(context)
    context.workerContext.offer {
        val promises = ArrayList<Promise<R, Exception>>()
        forEach {
            value ->
            promises add async(context) { bind(value) }
        }
        val promiseArray: Array<Promise<R, Exception>> = promises.toArray(arrayOf())
        val masterPromise = all(*promiseArray)
        masterPromise success {
            deferred resolve it
        }
        masterPromise fail {
            deferred reject it
        }
    }

    return deferred.promise
}