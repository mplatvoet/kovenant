package examples.example02

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.asExecutorService
import support.fib
import java.util.Random
import java.util.concurrent.Callable

fun main(args: Array<String>) {
    val executorService = Kovenant.context.workerDispatcher.asExecutorService()

    val random = Random()
    val tasks = listOf(*(Array(5) { FibCallable(random.nextInt(25)) }))


    val (n, fib) = executorService.invokeAny(tasks)
    println("invokeAny: fib($n) = $fib")
    println()

    val results = executorService.invokeAll(tasks)
    results forEach { future ->
        val (i, res) = future.get()
        println("invokeAll: fib($i) = $res")
    }

    //Not necessary but shuts down a bit quicker
    executorService.shutdownNow()
}


private class FibCallable(private val n: Int) : Callable<Pair<Int, Int>> {
    override fun call() = Pair(n, fib(n))
}


