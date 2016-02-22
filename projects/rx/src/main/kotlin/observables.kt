package nl.komponents.kovenant.rx

import nl.komponents.kovenant.*
import rx.Observable
import rx.Subscriber
import java.util.*


enum class ElementStrategy {
    FIRST, LAST
}

fun <V> resolvePolicy(default: () -> V): EmptyPolicy<V, Exception> = ResolvePolicy(default)
fun <V> rejectPolicy(default: () -> Exception): EmptyPolicy<V, Exception> = RejectPolicy(default)

fun <V> Observable<V>.toPromise(context: Context = Kovenant.context,
                                strategy: ElementStrategy = ElementStrategy.FIRST,
                                emptyFactory: () -> V): Promise<V, Exception> {
    return toPromise(context, strategy, resolvePolicy(emptyFactory))
}

fun <V> Observable<V>.toPromise(context: Context = Kovenant.context,
                                strategy: ElementStrategy = ElementStrategy.FIRST,
                                emptyPolicy: EmptyPolicy<V, Exception> = EmptyPolicy.getDefault()): Promise<V, Exception> {
    val subscriber: PromiseSubscriber<V> = when (strategy) {
        ElementStrategy.FIRST -> FirstValueSubscriber(context, emptyPolicy)
        ElementStrategy.LAST -> LastValueSubscriber(context, emptyPolicy)
    }
    subscribe(subscriber)
    return subscriber.promise
}

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
            deferred.resolve(value as V)
        } else {
            emptyPolicy.apply(deferred)
        }
    }
}

interface EmptyPolicy<V, E> {
    companion object {
        private val default = RejectPolicy<Any>() {
            EmptyException("completed without elements")
        }

        @Suppress("UNCHECKED_CAST")
        fun <V> getDefault(): EmptyPolicy<V, Exception> = default as EmptyPolicy<V, Exception>
    }

    fun apply(deferred: Deferred<V, E>)
}

private class RejectPolicy<V>(private val factory: () -> Exception) : EmptyPolicy<V, Exception> {

    override fun apply(deferred: Deferred<V, Exception>) {
        deferred.reject(factory())
    }
}

private class ResolvePolicy<V>(private val factory: () -> V) : EmptyPolicy<V, Exception> {
    override fun apply(deferred: Deferred<V, Exception>) {
        deferred.resolve(factory())
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

