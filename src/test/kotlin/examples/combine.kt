package examples.combine

import nl.mplatvoet.komponents.kovenant.async
import nl.mplatvoet.komponents.kovenant.combine
import support.fib

fun main(args: Array<String>) {
    val fib20Promise = async { fib(20) }
    val helloWorldPromise = async {"hello world"}

    val combinedPromise = combine(fib20Promise, helloWorldPromise)
    combinedPromise.success {
        val (fib, msg) = it
        println("$msg, fib(20) = $fib")
    }
}

