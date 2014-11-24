package nl.mplatvoet.kotlin.komponents.promises

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.RejectedExecutionException
import kotlin.InlineOption.ONLY_LOCAL_RETURN

/**
 * Created by mplatvoet on 22-4-2014.
 */

public trait Deferred<in V, in E> {
    fun resolve(value: V)
    fun reject(error: E)
    val promise: Promise<V, E>
}


public trait Promise<V, E> {
    fun success(callback: (value: V) -> Unit): Promise<V, E>
    fun fail(callback: (error: E) -> Unit): Promise<V, E>
    fun always(callback: () -> Unit): Promise<V, E>
}

public fun Promises.newDeferred<V, E>(config: Configuration = Promises.configuration) : Deferred<V, E> = DeferredPromise(config)

public fun Promises.async<V>(config: Configuration = Promises.configuration, body: () -> V): Promise<V, Exception> {
    val deferred = DeferredPromise<V, Exception>(config)
    config.tryExecute {
        try {
            val result = body()
            deferred.resolve(result)
        } catch(e: Exception) {
            deferred.reject(e)
        }
    }
    return deferred
}

private inline fun Configuration.tryExecute(inlineOptions(ONLY_LOCAL_RETURN) body: () -> Unit) {
    try {
        executor.execute { body() }
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

public fun <V, R> Promise<V, Exception>.then(config: Configuration = Promises.configuration, bind: (V) -> R): Promise<R, Exception> {
    val deferred = DeferredPromise<R, Exception>(config)
    success {
        try {
            val result = bind(it)
            deferred.resolve(result)
        } catch(e: Exception) {
            deferred.reject(e)
        }
    }
    fail {
        deferred.reject(it)
    }
    return deferred
}


private class DeferredPromise<out V, out E>(private val config: Configuration) : Promise<V, E>, ResultVisitor<V, E>, Deferred<V, E>  {
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
            throw IllegalStateException("result can only be set once")
        }
    }

    override fun visitValue(value: V) = fire(successCallbacks, value)
    override fun visitError(error: E) = fire(failCallbacks, error)

    override fun success(callback: (value: V) -> Unit): Promise<V, E> {
        val result = resultRef.get()
        if (result != null) {
            if (result is ValueResult) config.tryExecute { callback(result.value) }
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
            if (result is ErrorResult) config.tryExecute { callback(result.error) }
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
            config.tryExecute { callback() }
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
                config.tryExecute { function(value) }
            }
        }
    }

    private fun fire(ref: AtomicReference<ValueNode<() -> Unit>>) {
        ref.iterate {
            if (!it.done && it.trySetDone()) {
                val function = it.value
                config.tryExecute { function() }
            }
        }
    }
}










