package examples.combine

import nl.mplatvoet.komponents.kovenant.and
import nl.mplatvoet.komponents.kovenant.async
import support.fib

fun main(args: Array<String>) {
    val fib20Promise = async { fib(20) }
    val helloWorldPromise = async { "hello world" }

    fib20Promise and helloWorldPromise success {
        val (fib, msg) = it
        println("$msg, fib(20) = $fib")
    }

    async { fib(20) } and async { "hello world" } success {
        println("${it.second}, fib(20) = ${it.first}")
    }
}

