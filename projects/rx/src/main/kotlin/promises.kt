/*
 * Copyright (c) 2016 Mark Platvoet<mplatvoet@gmail.com>
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
package nl.komponents.kovenant.rx

import nl.komponents.kovenant.Promise
import rx.Observable
import rx.Producer
import rx.Subscriber
import rx.exceptions.Exceptions
import java.util.concurrent.atomic.AtomicInteger


fun <V, E : Exception> Promise<V, E>.toObservable(): Observable<V> = Observable.create(PromiseOnSubscribe(this))

private class PromiseOnSubscribe<V, E : Exception>(private val promise: Promise<V, E>) : Observable.OnSubscribe<V> {

    override fun call(subscriber: Subscriber<in V>) {
        val producer = PromiseProducer<V, E>(subscriber)
        subscriber.setProducer(producer)
        if (promise.isDone()) {
            if (promise.isSuccess()) producer.setValue(promise.get())
            else if (promise.isFailure()) producer.setError(promise.getError())
        } else {
            promise success { producer.setValue(it) }
            promise fail { producer.setError(it) }
        }
    }

    private class PromiseProducer<V, E : Exception>(private val subscriber: Subscriber<in V>) : Producer {
        companion object {
            val error_result = 4
            val value_result = 2
            val has_request = 1
        }

        private @Volatile var value: Any? = null
        private val state = AtomicInteger(0)

        override fun request(n: Long) {
            if (n < 0) throw IllegalArgumentException("n >= 0 required")
            if (n > 0) {
                while (true) {
                    val oldState = state.get()
                    if (oldState.isRequestSet()) return // request is already set, so return

                    val newState = oldState or has_request
                    if (state.compareAndSet(oldState, newState)) {
                        if (oldState.isResolved()) emit()
                        return // we are done
                    }
                }
            }
        }

        private fun Int.isError() = hasFlag(error_result)
        private fun Int.isValue() = hasFlag(value_result)

        private fun Int.isResolved() = this >= value_result // yeah nasty right ;-)

        private fun Int.isRequestSet() = hasFlag(has_request)

        private fun Int.hasFlag(flag: Int) = this and flag == flag

        fun setError(error: E) {
            value = error
            setResolvedState(error_result)
        }

        fun setValue(value: V) {
            this.value = value
            setResolvedState(value_result)
        }

        private fun setResolvedState(flag: Int) {
            while (true) {
                val oldState = state.get()
                val newState = oldState or flag
                if (state.compareAndSet(oldState, newState)) {
                    if (oldState.isRequestSet()) emit()

                    return // we are done
                }
                if (oldState.isResolved()) {
                    //sanity check against poor implemented Promises
                    throw IllegalStateException("It shouldn't happen, but did...")
                }
            }
        }

        //This behaviour is mimicked from SingleDelayedProducer
        private fun emit() {
            val s = state.get()
            when {
                s.isValue() -> emitValue()
                s.isError() -> emitError()
                else -> throw IllegalStateException("It shouldn't happen, but did...")
            }
        }

        private fun emitError() {
            @Suppress("UNCHECKED_CAST")
            val e = value as E
            Exceptions.throwIfFatal(e)
            subscriber.onError(e)
        }

        //This behaviour is mimicked from SingleDelayedProducer
        private fun emitValue() {
            if (!subscriber.isUnsubscribed) {
                @Suppress("UNCHECKED_CAST")
                val v = value as V
                try {
                    subscriber.onNext(v)
                } catch (e: Throwable) {
                    Exceptions.throwOrReport(e, subscriber, v)
                    return
                }
            }
            if (!subscriber.isUnsubscribed) {
                subscriber.onCompleted()
            }
        }
    }
}

