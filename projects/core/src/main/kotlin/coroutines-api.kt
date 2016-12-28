package nl.komponents.kovenant

import kotlin.coroutines.Continuation
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

fun <V> async(context: Context = Kovenant.context,
              body: suspend () -> V): Promise<V, Exception> {
    val deferred = deferred<V, Exception>(context)

    body.startCoroutine(object : Continuation<V>{
        override fun resumeWithException(exception: Throwable) {
            deferred.reject(Exception(exception))
        }

        override fun resume(value: V) {
            deferred.resolve(value)
        }
    })

    return deferred.promise
}

suspend fun <T> await(promise: Promise<T, Exception>) = suspendCoroutine<T> { c ->
    if (promise.isDone()) {
        when {
            promise.isSuccess() -> c.resume(promise.get())
            promise.isFailure() -> c.resumeWithException(promise.getError())
        }
    } else {
        promise success {
            c.resume(it)
        }
        promise fail {
            c.resumeWithException(it)
        }
    }
}