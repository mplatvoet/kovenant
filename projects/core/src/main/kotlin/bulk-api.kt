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
 * Creates a promise that is considered done if either all provided promises are successful or one fails.
 *
 * Creates a promise that is considered done if either all provided promises are successful or one fails. If all provided
 * promises are successful this promise resolves with a `List<V>` of all the results. The order if the items in the list
 * is the same order as the provided promises.
 *
 * If one or multiple promises result in failure this promise fails too. The error value of the first promise that fails
 * will be the cause of fail of this promise. If [cancelOthersOnError] is `true` it is attempted to cancel the execution of
 * the other promises.
 *
 * If provided no promises are provided an empty successful promise is returned.
 *
 * @param promises the promises that create the new combined promises of.
 * @param context the context on which the newly created promise operates on. `Kovenant.context` by default.
 * @param cancelOthersOnError whether an error of one promise attempts to cancel the other (unfinished) promises. `true` by default
 */
public fun <V> all(vararg promises: Promise<V, Exception>,
                        context: Context = Kovenant.context,
                        cancelOthersOnError: Boolean = true): Promise<List<V>, Exception> {
    return when (promises.size) {
        0 -> Promise.ofSuccess(listOf(), context)
        else -> concreteAll(promises = *promises, context = context, cancelOthersOnError = cancelOthersOnError)
    }
}

/**
 * Creates a promise that is considered done if either all provided promises are successful or one fails.
 *
 * Creates a promise that is considered done if either all provided promises are successful or one fails. If all provided
 * promises are successful this promise resolves with a `List<V>` of all the results. The order if the items in the list
 * is the same order as the provided promises.
 *
 * If one or multiple promises result in failure this promise fails too. The error value of the first promise that fails
 * will be the cause of fail of this promise. If [cancelOthersOnError] is `true` it is attempted to cancel the execution of
 * the other promises.
 *
 * If provided no promises are provided an empty successful promise is returned.
 *
 * @param promises the List of promises that create the new combined promises of.
 * @param context the context on which the newly created promise operates on. `Kovenant.context` by default.
 * @param cancelOthersOnError whether an error of one promise attempts to cancel the other (unfinished) promises. `true` by default
 */
public fun <V> all(promises: List<Promise<V, Exception>>,
                        context: Context = Kovenant.context,
                        cancelOthersOnError: Boolean = true): Promise<List<V>, Exception> {
    return when (promises.size) {
        0 -> Promise.ofSuccess(listOf(), context)
        else -> concreteAll(promises = promises, context = context, cancelOthersOnError = cancelOthersOnError)
    }
}


/**
 * Creates a promise that is considered done if either any provided promises is successful or all promises have failed.
 *
 * Creates a promise that is considered done if either any provided promises is successful or all promises have failed.
 * If any of the provided promises is successful this promise resolves successful with that result.
 * If [cancelOthersOnSuccess] is `true` it is attempted to cancel the execution of the other promises.
 *
 * If all promises result in failure this promise fails too with a `List` containing all failures. The order if the
 * items in the list is the same order as the provided promises.
 *
 * If provided no promises are provided an empty failed promise is returned.
 *
 * @param promises the promises that create the new combined promises of.
 * @param context the context on which the newly created promise operates on. `Kovenant.context` by default.
 * @param cancelOthersOnSuccess whether a success of one promise attempts to cancel the other (unfinished) promises. `true` by default
 */
public fun <V> any(vararg promises: Promise<V, Exception>,
                        context: Context = Kovenant.context,
                        cancelOthersOnSuccess: Boolean = true): Promise<V, List<Exception>> {
    return when (promises.size) {
        0 -> Promise.ofFail(listOf(), context)
        else -> concreteAny(promises = *promises, context = context, cancelOthersOnSuccess = cancelOthersOnSuccess)
    }
}

/**
 * Creates a promise that is considered done if either any provided promises is successful or all promises have failed.
 *
 * Creates a promise that is considered done if either any provided promises is successful or all promises have failed.
 * If any of the provided promises is successful this promise resolves successful with that result.
 * If [cancelOthersOnSuccess] is `true` it is attempted to cancel the execution of the other promises.
 *
 * If all promises result in failure this promise fails too with a `List` containing all failures. The order if the
 * items in the list is the same order as the provided promises.
 *
 * If provided no promises are provided an empty failed promise is returned.
 *
 * @param promises the List of promises that create the new combined promises of.
 * @param context the context on which the newly created promise operates on. `Kovenant.context` by default.
 * @param cancelOthersOnSuccess whether a success of one promise attempts to cancel the other (unfinished) promises. `true` by default
 */
public fun <V> any(promises: List<Promise<V, Exception>>,
                        context: Context = Kovenant.context,
                        cancelOthersOnSuccess: Boolean = true): Promise<V, List<Exception>> {
    return when (promises.size) {
        0 -> Promise.ofFail(listOf(), context)
        else -> concreteAny(promises = promises, context = context, cancelOthersOnSuccess = cancelOthersOnSuccess)
    }
}