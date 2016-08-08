package nl.komponents.kovenant

fun <V> async(context: Context = Kovenant.context,
              coroutine body: PromiseController<V>.() -> Continuation<Unit>): Promise<V, Exception> {
    val controller = PromiseController<V>(context)
    controller.body().resume(Unit)
    return controller.promise
}

class PromiseController<V>(context: Context) {

    private val deferred = deferred<V, Exception>(context)
    val promise = deferred.promise

    suspend fun <T> await(promise: Promise<T, Exception>,
                          c: Continuation<T>) {
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

    operator fun handleResult(value: V, c: Continuation<Nothing>) {
        deferred.resolve(value)
    }

    operator fun handleException(t: Throwable, c: Continuation<Nothing>) {
        deferred.reject(Exception(t))
    }
}
