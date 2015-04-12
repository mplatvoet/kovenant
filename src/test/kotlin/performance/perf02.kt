package performance.validate03

import nl.mplatvoet.komponents.kovenant.*
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

val numberOfWorkerThreads = Runtime.getRuntime().availableProcessors()

val executorService = Executors.newFixedThreadPool(numberOfWorkerThreads)


fun main(args: Array<String>) {
    Kovenant.configure {
        workerDispatcher = buildDispatcher {
            numberOfThreads = numberOfWorkerThreads
        }
    }

    val attempts = 10
    val warmupRounds = 100000
    val performanceRounds = 3000000

    val factors = ArrayList<Double>(attempts)
    for (i in 1..10) {
        validateFutures(warmupRounds)

        val startExc = System.currentTimeMillis()
        validateFutures(performanceRounds)
        val deltaExc = System.currentTimeMillis() - startExc

        validatePromises(warmupRounds)

        val startDis = System.currentTimeMillis()
        validatePromises(performanceRounds)
        val deltaDis = System.currentTimeMillis() - startDis

        val factor = deltaExc.toDouble() / deltaDis.toDouble()
        factors add factor
        println("[$i/$attempts] Callables: ${deltaExc}ms, Promises: ${deltaDis}ms. " +
                "Promises are a factor ${factor.format("##0.00")} ${fasterOrSlower(factor)}")
    }

    val averageFactor = factors.sum() / attempts.toDouble()
    println("On average with ${attempts} attempts, " +
            "Promises where a factor ${averageFactor.format("##0.00")} ${fasterOrSlower(averageFactor)}")

    executorService.shutdownNow()
}


fun validatePromises(n: Int) {
    val promises = Array(n) { n ->
        Kovenant.async {
            val i = 13
            Pair(i, fib(i))
        }
    }

    await(*promises)
}

fun validateFutures(n: Int) {
    val callables = ArrayList<Callable<Pair<Int, Int>>>(n)

    (1..n).forEach {
        n ->
        callables add Callable {
            val i = 13
            Pair(i, fib(i))
        }
    }
    executorService.invokeAll(callables)
}


//a very naive fibonacci implementation
fun fib(n: Int): Int {
    if (n < 0) throw IllegalArgumentException("negative numbers not allowed")
    return when (n) {
        0, 1 -> 1
        else -> fib(n - 1) + fib(n - 2)
    }
}

private fun await(vararg promises: Promise<*, *>) {
    val latch = CountDownLatch(promises.size())
    promises forEach {
        p ->
        p always { latch.countDown() }
    }
    latch.await()
}

private fun Double.format(pattern: String): String = DecimalFormat(pattern).format(this)

private fun fasterOrSlower(value: Double) = if (value < 1.0) "slower" else "faster"

