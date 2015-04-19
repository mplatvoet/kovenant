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

private class DeferredPromise<V, E>(override val context: Context) : AbstractPromise<V, E>(), Promise<V, E>, Deferred<V, E> {

    override fun resolve(value: V) {
        if (trySetSuccessResult(value)) {
            fireSuccess(value)
        } else {
            throw IllegalStateException("Promise already resolved")
        }
    }

    override fun reject(error: E) {
        if (trySetFailResult(error)) {
            fireFail(error)
        } else {
            throw IllegalStateException("Promise already resolved")
        }
    }

    override val promise: Promise<V, E> = this


    override fun success(callback: (value: V) -> Unit): Promise<V, E> {
        if (isFailResult()) return this;

        addSuccessCb(callback)

        //possibly resolved already
        if (isSuccessResult()) fireSuccess(getAsValueResult())

        return this
    }

    override fun fail(callback: (error: E) -> Unit): Promise<V, E> {
        if (isSuccessResult()) return this

        addFailCb(callback)

        //possibly rejected already
        if (isFailResult()) fireFail(getAsFailResult())

        return this
    }

    override fun always(callback: () -> Unit): Promise<V, E> {
        addAlwaysCb(callback)

        //possibly completed already
        when {
            isSuccessResult() -> fireSuccess(getAsValueResult())
            isFailResult() -> fireFail((getAsFailResult()))
        }

        return this
    }


    private fun fireSuccess(value: V) {
        do {
            val node = popSuccessCb()
            if (node != null) {
                context.tryDispatch {
                    node.runSuccess(value)
                }
            }
        } while (node != null)
    }

    private fun fireFail(value: E) {
        do {
            val node = popFailCb()
            if (node != null) {
                context.tryDispatch {
                    node.runFail(value)
                }
            }
        } while (node != null)
    }

}





