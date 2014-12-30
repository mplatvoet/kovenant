/*
 * Copyright (c) 2014-2015 Mark Platvoet<mplatvoet@gmail.com>
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package nl.mplatvoet.kotlin.komponents.promises

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.RejectedExecutionException
import kotlin.InlineOption.ONLY_LOCAL_RETURN

/**
 * Created by mplatvoet on 22-4-2014.
 */

public trait Deferred<V, E> {
    fun resolve(value: V)
    fun reject(error: E)
    val promise: Promise<V, E>
}


public trait Promise<V, E> {
    fun success(callback: (value: V) -> Unit): Promise<V, E>
    fun fail(callback: (error: E) -> Unit): Promise<V, E>
    fun always(callback: () -> Unit): Promise<V, E>
}

public fun Promises.newDeferred<V, E>(config: Context = Promises.configuration) : Deferred<V, E> = DeferredPromise(config)


private fun Context.tryDispatch(body: () -> Unit) {
    try {
        dispatchExecutor(body)
    } catch (e: Exception) {
        dispatchingError(e)
    }
}

private fun Context.tryWork(runnable: () -> Unit) {
    try {
        workExecutor(runnable)
    } catch (e: Exception) {
        dispatchingError(e)
    }
}

public fun Promises.async<V>(context: Context = Promises.configuration, body: () -> V): Promise<V, Exception> {
    val deferred = DeferredPromise<V, Exception>(context)
    context.tryWork {
        try {
            val result = body()
            deferred.resolve(result)
        } catch(e: Exception) {
            deferred.reject(e)
        }
    }
    return deferred
}

public fun <V, R> Promise<V, Exception>.then(context: Context = Promises.configuration, bind: (V) -> R): Promise<R, Exception> {
    val deferred = DeferredPromise<R, Exception>(context)
    success {
        context.tryWork {
            try {
                val result = bind(it)
                deferred.resolve(result)
            } catch(e: Exception) {
                deferred.reject(e)
            }
        }
    }
    fail {
        deferred.reject(it)
    }
    return deferred
}












