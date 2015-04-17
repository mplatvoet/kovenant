package examples

import nl.mplatvoet.komponents.kovenant.Promise
import nl.mplatvoet.komponents.kovenant.deferred

fun main(args: Array<String>) {
    val deferred = deferred<String,Exception>()
    handlePromise(deferred.promise)
    deferred.resolve("Hello World")
//    deferred.reject(Exception("Hello exceptional World"))
}
fun handlePromise(promise: Promise<String, Exception>) {
    promise success {
        msg -> println(msg)
    }
    promise fail {
        e -> println(e)
    }
}