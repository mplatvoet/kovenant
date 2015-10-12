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

import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray


internal fun <V> concreteAll(vararg promises: Promise<V, Exception>,
                           context: Context,
                           cancelOthersOnError: Boolean): Promise<List<V>, Exception> {
    return concreteAll(promises.asSequence(), promises.size(), context, cancelOthersOnError)
}

internal fun <V> concreteAll(promises: List<Promise<V, Exception>>,
                           context: Context,
                           cancelOthersOnError: Boolean): Promise<List<V>, Exception> {
    // this might fail with concurrent mutating list, revisit in the future
    // not really a Kovenant issue but can prevent this from ever completing
    return concreteAll(promises.asSequence(), promises.size, context, cancelOthersOnError)
}

internal fun <V> concreteAll(promises: Sequence<Promise<V, Exception>>,
                           sequenceSize: Int,
                           context: Context,
                           cancelOthersOnError: Boolean): Promise<List<V>, Exception> {
    if (sequenceSize == 0) throw IllegalArgumentException("no promises provided")

    val deferred = deferred<List<V>, Exception>(context)
    val results = AtomicReferenceArray<V>(sequenceSize)
    val successCount = AtomicInteger(sequenceSize)
    val failCount = AtomicInteger(0)
    promises.forEachIndexed {
        i, promise ->
        promise.success { v ->
            results.set(i, v)
            if (successCount.decrementAndGet() == 0) {

                deferred.resolve(results.asList())
            }
        }
        promise.fail { e ->
            if (failCount.incrementAndGet() == 1) {
                deferred.reject(e)
                if (cancelOthersOnError) {
                    promises.forEach {
                        if (it != promise && it is CancelablePromise) {
                            it.cancel(CancelException())
                        }
                    }
                }
            }
        }

    }

    return deferred.promise
}

internal fun <V> concreteAny(vararg promises: Promise<V, Exception>,
                           context: Context,
                           cancelOthersOnSuccess: Boolean): Promise<V, List<Exception>> {
    return concreteAny(promises.asSequence(), promises.size(), context, cancelOthersOnSuccess)
}

internal fun <V> concreteAny(promises: List<Promise<V, Exception>>,
                           context: Context,
                           cancelOthersOnSuccess: Boolean): Promise<V, List<Exception>> {
    // this might fail with concurrent mutating list, revisit in the future
    // not really a Kovenant issue but can prevent this from ever completing
    return concreteAny(promises.asSequence(), promises.size, context, cancelOthersOnSuccess)
}

internal fun <V> concreteAny(promises: Sequence<Promise<V, Exception>>,
                           sequenceSize: Int,
                           context: Context,
                           cancelOthersOnSuccess: Boolean): Promise<V, List<Exception>> {
    if (sequenceSize == 0) throw IllegalArgumentException("no promises provided")

    val deferred = deferred<V, List<Exception>>(context)
    val errors = AtomicReferenceArray<Exception>(sequenceSize)
    val successCount = AtomicInteger(0)
    val failCount = AtomicInteger(sequenceSize)

    promises.forEachIndexed {
        i, promise ->
        promise.success { v ->
            if (successCount.incrementAndGet() == 1) {
                deferred.resolve(v)
                if (cancelOthersOnSuccess) {
                    promises.forEach {
                        if (it != promise && it is CancelablePromise) {
                            it.cancel(CancelException())
                        }
                    }
                }
            }
        }
        promise.fail { e ->
            errors.set(i, e)
            if (failCount.decrementAndGet() == 0) {
                deferred.reject(errors.asList())
            }
        }

    }

    return deferred.promise
}

private fun <V> AtomicReferenceArray<V>.asList(): List<V> {
    val list = ArrayList<V>()
    for (i in 0..this.length() - 1) {
        list.add(this.get(i))
    }
    return list
}
