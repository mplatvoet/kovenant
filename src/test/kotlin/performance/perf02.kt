package performance.perf02

import nl.mplatvoet.komponents.kovenant.*
import support.fib
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val numberOfWorkerThreads = Runtime.getRuntime().availableProcessors()
val executorService = Executors.newFixedThreadPool(numberOfWorkerThreads)
val attempts = 10
val warmupRounds = 100000
val performanceRounds = 1000000
val napTimeSeconds = 3L
val fibN = 13

fun main(args: Array<String>) {
    println(
            """Performance test
- samples:      $attempts
- warmupRounds: $warmupRounds
- timingRounds: $performanceRounds
- workers:      $numberOfWorkerThreads
- sleep:        $napTimeSeconds seconds
""")

    Kovenant.configure {
        workerDispatcher = buildDispatcher {
            numberOfThreads = numberOfWorkerThreads
        }
    }


    val factors = ArrayList<Double>(attempts)
    for (i in 1..attempts) {
        validateFutures(warmupRounds)

        val startExc = System.currentTimeMillis()
        validateFutures(performanceRounds)
        val deltaExc = System.currentTimeMillis() - startExc
        napTime()

        validatePromises(warmupRounds)

        val startDis = System.currentTimeMillis()
        validatePromises(performanceRounds)
        val deltaDis = System.currentTimeMillis() - startDis

        val factor = deltaExc.toDouble() / deltaDis.toDouble()
        factors add factor
        println("[$i/$attempts] Callables: ${deltaExc}ms, Promises: ${deltaDis}ms. " +
                "Promises are a factor ${factor.format("##0.00")} ${fasterOrSlower(factor)}")
        napTime()
    }

    val averageFactor = factors.sum() / attempts.toDouble()
    println("On average with ${attempts} attempts, " +
            "Promises where a factor ${averageFactor.format("##0.00")} ${fasterOrSlower(averageFactor)}")

    executorService.shutdownNow()
}

private fun napTime() {
    System.gc()
    Thread.sleep(TimeUnit.MILLISECONDS.convert(napTimeSeconds, TimeUnit.SECONDS))
}


fun validatePromises(n: Int) {
    val promises = Array(n) { n ->
        async {
            Pair(fibN, fib(fibN))
        }
    }

    await(*promises)
}

fun validateFutures(n: Int) {
    val callables = ArrayList<Callable<Pair<Int, Int>>>(n)

    (1..n).forEach {
        n ->
        callables add Callable {

            Pair(fibN, fib(fibN))
        }
    }
    executorService.invokeAll(callables)
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