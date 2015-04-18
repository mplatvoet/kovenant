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

private class DeferredPromise<V, E>(private val config: Context) : AbstractPromise<V, E>(), Promise<V, E>, Deferred<V, E> {


    override fun resolve(value: V) {
        if (trySetSuccessResult(value)) {
            fire(successCbs, value)
            fire(alwaysCbs)
        } else {
            throw IllegalStateException("Promise already resolved")
        }
    }

    override fun reject(error: E) {
        if (trySetFailResult(error)) {
            fire(failCbs, error)
            fire(alwaysCbs)
        } else {
            throw IllegalStateException("Promise already resolved")
        }
    }

    override val promise: Promise<V, E> = this


    override fun success(callback: (value: V) -> Unit): Promise<V, E> {
        if (isSuccessResult()) {
            val v = getAsValueResult()
            config.tryDispatch { callback(v) }
        } else {
            addSuccessCb(callback)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            if (isSuccessResult()) {
                val v = getAsValueResult()
                fire (successCbs, v)
            }
        }

        return this
    }

    override fun fail(callback: (error: E) -> Unit): Promise<V, E> {
        if (isFailResult()) {
            val e = getAsFailResult()
            config.tryDispatch { callback(e) }
        } else {
            addFailCb(callback)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            if (isFailResult()) fire (failCbs, getAsFailResult())
        }

        return this
    }

    override fun always(callback: () -> Unit): Promise<V, E> {

        if (isCompleted()) {
            config.tryDispatch { callback() }
        } else {
            addAlwaysCb(callback)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            if (isCompleted()) fire (alwaysCbs)
        }

        return this
    }

    private fun fire<T>(ref: ValueNode<(T) -> Unit>?, value: T) {

        ref.iterate {
            if (!it.marked && it.tryMark()) {

                val function = it.value
                config.tryDispatch {
                    function(value)
                }
            }
        }
    }

    private fun fire(ref: ValueNode<() -> Unit>?) {
        ref.iterate {
            if (!it.marked && it.tryMark()) {
                val function = it.value
                config.tryDispatch { function() }
            }
        }
    }
}

private inline fun <T> ValueNode<T>?.iterate(cb: (ValueNode<T>) -> Unit) {
    var node = this
    while (node != null) {
        val n = node as ValueNode<T>
        cb(n)
        node = n.next
    }
}

private val <T : Any> ValueNode<T>.next: ValueNode<T>? get() = this.getNext()
private val <T : Any> ValueNode<T>.marked: Boolean get() = this.isMarked()
private val <T : Any> ValueNode<T>.value: T get() = this.getValue()


