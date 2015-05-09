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

import java.util.ArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray


private fun concreteAll<V, E>(vararg promises: Promise<V, E>): Promise<List<V>, E> {
    if (promises.size() == 0) throw IllegalArgumentException("no promises provided")

    val deferred = deferred<List<V>, E>()
    val results = AtomicReferenceArray<V>(promises.size())
    val successCount = AtomicInteger(promises.size())
    val failCount = AtomicInteger(0)
    promises.forEachIndexed {
        i, promise ->
        promise.success { v ->
            results[i] = v
            if (successCount.decrementAndGet() == 0) {

                deferred.resolve(results.asList())
            }
        }
        promise.fail { e ->
            if (failCount.incrementAndGet() == 1) {
                deferred.reject(e)
            }
        }

    }

    return deferred.promise
}

private fun concreteAny<V, E>(vararg promises: Promise<V, E>): Promise<V, List<E>> {
    if (promises.size() == 0) throw IllegalArgumentException("no promises provided")

    val deferred = deferred<V, List<E>>()
    val results = AtomicReferenceArray<E>(promises.size())
    val successCount = AtomicInteger(0)
    val failCount = AtomicInteger(promises.size())

    promises.forEachIndexed {
        i, promise ->
        promise.success { v ->
            if (successCount.incrementAndGet() == 1) {
                deferred.resolve(v)
            }
        }
        promise.fail { e ->
            results[i] = e
            if (failCount.decrementAndGet() == 0) {
                deferred.reject(results.asList())
            }
        }

    }

    return deferred.promise
}

private fun <V> AtomicReferenceArray<V>.asList(): List<V> {
    val list = ArrayList<V>()
    for (i in 0..this.length() - 1) {
        list add this[i]
    }
    return list
}