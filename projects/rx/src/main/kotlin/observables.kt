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

import nl.komponents.kovenant.*
import rx.Observable
import rx.Subscriber
import java.util.*


enum class EmitStrategy {
    FIRST, LAST
}

/**
 * Turn an `Observable<V>` into a `Promise<V, Exception>`
 *
 * @param context the context of the new promise, `Kovenant.context` by default
 * @param strategy the `EmitStrategy` to be used, `EmitStrategy.FIRST` by default
 * @param emptyFactory the value to be calculated and used if the `Observable` hasn't emitted any values
 */
fun <V> Observable<V>.toPromise(context: Context = Kovenant.context,
                                strategy: EmitStrategy = EmitStrategy.FIRST,
                                emptyFactory: () -> V)
        : Promise<V, Exception> = toPromise(context, strategy, EmptyPolicy.resolve(emptyFactory))

/**
 * Turn an `Observable<V>` into a `Promise<V, Exception>`
 *
 * @param context the context of the new promise, `Kovenant.context` by default
 * @param strategy the `EmitStrategy` to be used, `EmitStrategy.FIRST` by default
 * @param emptyPolicy to policy to be used when the `Observable` hasn't emitted any values, rejection by default
 */
fun <V> Observable<V>.toPromise(context: Context = Kovenant.context,
                                strategy: EmitStrategy = EmitStrategy.FIRST,
                                emptyPolicy: EmptyPolicy<V, Exception> = EmptyPolicy.default()): Promise<V, Exception> {
    val subscriber: PromiseSubscriber<V> = when (strategy) {
        EmitStrategy.FIRST -> FirstValueSubscriber(context, emptyPolicy)
        EmitStrategy.LAST -> LastValueSubscriber(context, emptyPolicy)
    }
    subscribe(subscriber)
    return subscriber.promise
}


/**
 * Turn an `Observable<V>` into a `Promise<V, Exception>`
 *
 * @param context the context of the new promise, `Kovenant.context` by default
 * @param strategy the `EmitStrategy` to be used, `EmitStrategy.FIRST` by default
 * @param defaultValue the value to be used when the `Observable` hasn't emitted any values
 */
fun <V> Observable<V>.toPromise(defaultValue: V, context: Context = Kovenant.context,
                                strategy: EmitStrategy = EmitStrategy.FIRST)
        : Promise<V, Exception> = toPromise(context, strategy, EmptyPolicy.resolve(defaultValue))

/**
 * Turn an `Observable<V>` into a `Promise<List<V>, Exception>`
 *
 * @param context the context of the new promise, `Kovenant.context` by default
 */
fun <V> Observable<V>.toListPromise(context: Context = Kovenant.context): Promise<List<V>, Exception> {
    val observer = ListValuesSubscriber<V>(context)
    subscribe(observer)
    return observer.promise
}

private abstract class PromiseSubscriber<V> : Subscriber<V>() {
    abstract val promise: Promise<V, Exception>
}

private class FirstValueSubscriber<V>(context: Context,
                                      private val emptyPolicy: EmptyPolicy<V, Exception>) : PromiseSubscriber<V>() {
    private val deferred = deferred<V, Exception>(context)
    override val promise: Promise<V, Exception> = deferred.promise

    override fun onNext(value: V) {
        deferred.resolve(value)
        unsubscribe()
    }

    override fun onError(error: Throwable?) = deferred.reject(error.asException())
    override fun onCompleted() = emptyPolicy.apply(deferred)
}

private class LastValueSubscriber<V>(context: Context,
                                     private val emptyPolicy: EmptyPolicy<V, Exception>) : PromiseSubscriber<V>() {
    //Rx always executes on same thread/worker, no need to sync
    private var value: V? = null
    private var hasValue = false

    private val deferred = deferred<V, Exception>(context)
    override val promise: Promise<V, Exception> = deferred.promise

    override fun onNext(value: V) {
        this.value = value
        hasValue = true
    }

    override fun onError(error: Throwable?) = deferred.reject(error.asException())
    override fun onCompleted() {
        if (hasValue) {
            @Suppress("UNCHECKED_CAST")
            deferred.resolve(value as V)
        } else {
            emptyPolicy.apply(deferred)
        }
    }
}

/**
 * EmptyPolicy describes a strategy to apply when Observables are completed but without
 * emitting any values
 */
interface EmptyPolicy<V, E> {
    companion object {
        private val default = FactoryRejectPolicy<Any>() {
            EmptyException("completed without elements")
        }

        @Suppress("UNCHECKED_CAST")
        fun <V> default(): EmptyPolicy<V, Exception> = default as EmptyPolicy<V, Exception>

        /**
         * Creates an `EmptyPolicy` that resolves as successful with the value
         * generated by the `factory`
         */
        fun <V> resolve(factory: () -> V): EmptyPolicy<V, Exception> = FactoryResolvePolicy(factory)

        /**
         * Creates an `EmptyPolicy` that resolves as successful with the provided value
         */
        fun <V> resolve(value: V): EmptyPolicy<V, Exception> = ValueResolvePolicy(value)

        /**
         * Creates an `EmptyPolicy` that resolves as failed with the error
         * generated by the `factory`
         */
        fun <V> reject(factory: () -> Exception): EmptyPolicy<V, Exception> = FactoryRejectPolicy(factory)

        /**
         * Creates an `EmptyPolicy` that resolves as failed with the provided error
         */
        fun <V> reject(error: Exception): EmptyPolicy<V, Exception> = ValueRejectPolicy(error)
    }

    fun apply(deferred: Deferred<V, E>)
}

private class FactoryRejectPolicy<V>(private val factory: () -> Exception) : EmptyPolicy<V, Exception> {

    override fun apply(deferred: Deferred<V, Exception>) {
        deferred.reject(factory())
    }
}

private class FactoryResolvePolicy<V>(private val factory: () -> V) : EmptyPolicy<V, Exception> {
    override fun apply(deferred: Deferred<V, Exception>) {
        deferred.resolve(factory())
    }
}

private class ValueResolvePolicy<V>(private val value: V) : EmptyPolicy<V, Exception> {
    override fun apply(deferred: Deferred<V, Exception>) {
        deferred.resolve(value)
    }
}

private class ValueRejectPolicy<V>(private val error: Exception) : EmptyPolicy<V, Exception> {
    override fun apply(deferred: Deferred<V, Exception>) {
        deferred.reject(error)
    }
}


private class ListValuesSubscriber<V>(context: Context) : Subscriber<V>() {
    //Rx always executes on same thread/worker, no need to sync
    private var values: MutableList<V> = ArrayList()

    private val deferred = deferred<List<V>, Exception>(context)
    val promise: Promise<List<V>, Exception> = deferred.promise

    override fun onNext(value: V) {
        values.add(value)
    }

    override fun onError(error: Throwable?) = deferred.reject(error.asException())
    override fun onCompleted() = deferred.resolve(values)
}


fun Throwable?.asException(): Exception = if (this is Exception) this else RuntimeException(this)

