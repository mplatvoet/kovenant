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

import kotlin.InlineOption.ONLY_LOCAL_RETURN
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicReference

private fun Context.tryDispatch(body: () -> Unit) {
    try {
        dispatchExecutor( body )
    } catch (e: RejectedExecutionException) {
        if (fallbackOnCurrentThread) {
            try {
                body()
            } catch(e: Exception) {
                executionErrors(e)
            }
        } else {
            executionErrors(e)
        }
    } catch (e: Exception) {
        executionErrors(e)
    }
}

private class DeferredPromise<V, E>(private val config: Context) : Promise<V, E>, ResultVisitor<V, E>, Deferred<V, E>  {
    private val successCallbacks = AtomicReference<ValueNode<(V) -> Unit>>()
    private val failCallbacks = AtomicReference<ValueNode<(E) -> Unit>>()
    private val alwaysCallbacks = AtomicReference<ValueNode<() -> Unit>>()

    private val resultRef = AtomicReference<Result<V, E>>()

    override fun resolve(value: V) = setResult(ValueResult(value))
    override fun reject(error: E) = setResult(ErrorResult(error))

    override val promise: Promise<V, E> = this

    private fun setResult(result: Result<V, E>) {
        if (this.resultRef.compareAndSet(null, result)) {
            result.accept(this)
            fire (alwaysCallbacks)
        } else {
            config.multipleCompletion(resultRef.get(), result.rawValue)
        }
    }

    override fun visitValue(value: V) = fire(successCallbacks, value)
    override fun visitError(error: E) = fire(failCallbacks, error)

    override fun success(callback: (value: V) -> Unit): Promise<V, E> {
        val result = resultRef.get()
        if (result != null) {
            if (result is ValueResult) config.tryDispatch { callback(result.value) }
        } else {
            successCallbacks.add(callback)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            val result2 = resultRef.get()
            if (result2 != null && result2 is ValueResult) fire (successCallbacks, result2.value)
        }

        return this
    }

    override fun fail(callback: (error: E) -> Unit): Promise<V, E> {
        val result = resultRef.get()
        if (result != null) {
            if (result is ErrorResult) config.tryDispatch { callback(result.error) }
        } else {
            failCallbacks.add(callback)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            val result2 = resultRef.get()
            if (result2 != null && result2 is ErrorResult) fire (failCallbacks, result2.error)
        }

        return this
    }

    override fun always(callback: () -> Unit): Promise<V, E> {
        val result = resultRef.get()
        if (result != null) {
            config.tryDispatch { callback() }
        } else {
            alwaysCallbacks.add(callback)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            val result2 = resultRef.get()
            if (result2 != null) fire (alwaysCallbacks)
        }

        return this
    }

    private fun fire<T>(ref: AtomicReference<ValueNode<(T) -> Unit>>, value: T) {
        ref.iterate {
            if (!it.done && it.trySetDone()) {
                val function = it.value
                config.tryDispatch { function(value) }
            }
        }
    }

    private fun fire(ref: AtomicReference<ValueNode<() -> Unit>>) {
        ref.iterate {
            if (!it.done && it.trySetDone()) {
                val function = it.value
                config.tryDispatch { function() }
            }
        }
    }
}


