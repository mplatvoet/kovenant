package nl.mplatvoet.kotlin.komponents.promises

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.RejectedExecutionException

/**
 * Created by mplatvoet on 22-4-2014.
 */

public fun Promises.defer<T>(config: Configuration = Promises.configuration, body: () -> T): Promise<T> {
    val obligation = WaitFreeObligation<T>(config)
    config.tryExecute {
        try {
            val result = body()
            obligation.fulfil(result)
        } catch(e: Exception) {
            obligation.cancel(e)
        }
    }
    return obligation.promise
}

private inline fun Configuration.tryExecute(body: () -> Unit) {
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

public fun <T, R> Promise<T>.then(config: Configuration = Promises.configuration, fn: (T) -> R): Promise<R> {
    val obligation = WaitFreeObligation<R>(config)
    success {
        try {
            obligation.fulfil(fn(it))
        } catch(e: Exception) {
            obligation.cancel(e)
        }
    }
    fail {
        obligation.cancel(it)
    }
    return obligation.promise
}



public trait Obligation<in T> {
    fun fulfil(value: T)
    fun cancel(e: Exception)
    val promise: Promise<T>
}


public trait Promise<T> {
    fun success(fn: (T) -> Unit): Promise<T>
    fun fail(fn: (Exception) -> Unit): Promise<T>
    fun always(fn: () -> Unit): Promise<T>
}

private class WaitFreeObligation<in T>(private val config: Configuration) : Obligation<T> {
    private val mutablePromise = MutablePromise<T>(config)
    override val promise: Promise<T> = mutablePromise

    override fun fulfil(value: T) = mutablePromise.setResult(ValueResult(value))
    override fun cancel(e: Exception) = mutablePromise.setResult(ExceptionResult(e))
}



private class MutablePromise<out T>(private val config: Configuration) : Promise<T>, ResultVisitor<T> {

    private val keepers = AtomicReference<Node<(T) -> Unit>>()
    private val breakers = AtomicReference<Node<(Exception) -> Unit>>()
    private val always = AtomicReference<Node<() -> Unit>>()

    private val resultRef = AtomicReference<Result<T>>()

    public fun setResult(result: Result<T>) {
        if (this.resultRef.compareAndSet(null, result)) {
            result.accept(this)
            fire (always)
        } else {
            throw IllegalStateException("result can only be set once")
        }
    }

    override fun visitValue(value: T) = fire(keepers, value)
    override fun visitException(e: Exception) = fire(breakers, e)

    override fun success(fn: (T) -> Unit): Promise<T> {
        val result = resultRef.get()
        if (result != null) {
            if (result is ValueResult) config.tryExecute { fn(result.value) }
        } else {
            keepers.add(fn)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            val result2 = resultRef.get()
            if (result2 != null && result2 is ValueResult) fire (keepers, result2.value)
        }

        return this
    }

    override fun fail(fn: (Exception) -> Unit): Promise<T> {
        val result = resultRef.get()
        if (result != null) {
            if (result is ExceptionResult) config.tryExecute { fn(result.e) }
        } else {
            breakers.add(fn)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            val result2 = resultRef.get()
            if (result2 != null && result2 is ExceptionResult) fire (breakers, result2.e)
        }

        return this
    }

    override fun always(fn: () -> Unit): Promise<T> {
        val result = resultRef.get()
        if (result != null) {
            config.tryExecute { fn() }
        } else {
            always.add(fn)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            val result2 = resultRef.get()
            if (result2 != null) fire (always)
        }

        return this
    }

    private fun fire<T>(ref: AtomicReference<Node<(T) -> Unit>>, value: T) {
        iterate(ref.get()) {
            if (it.trySetDone()) {
                config.tryExecute { it.value(value) }
            }
        }
    }

    private fun fire(ref: AtomicReference<Node<() -> Unit>>) {
        iterate(ref.get()) {
            if (it.trySetDone()) {
                config.tryExecute { it.value() }
            }
        }
    }
}


// A very basic linked list implementation.
// It's non blocking thread safe but items can only be added
// Build with low memory footprint in mind
private inline fun iterate<T>(head: Node<T>?, cb: (Node<T>) -> Unit) {
    var node = head
    while (node != null) {
        val n = node!!
        cb(n)
        node = n.next
    }
}

private fun <T> AtomicReference<Node<T>>.add(value: T) {
    val node = Node(value)
    if (!compareAndSet(null, node)) {
        var n = get()!!
        while (!n.trySetNext(node)) n = n.next!!
    }
}

private data class Node<T>(val value: T) {
    private val _done = AtomicBoolean(false)
    val done: Boolean get() = _done.get()
    fun trySetDone(): Boolean = _done.compareAndSet(false, true)

    private val _next = AtomicReference<Node<T>>(null)
    fun trySetNext(node: Node<T>): Boolean = _next.get() == null && _next.compareAndSet(null, node)
    val next: Node<T>? = _next.get()
}






