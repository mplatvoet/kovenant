package performance.perf01

import nl.mplatvoet.komponents.kovenant.*
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


val numberOfWorkerThreads = Runtime.getRuntime().availableProcessors()
val excWorkDispatcher = Executors.newFixedThreadPool(numberOfWorkerThreads).asDispatcher()
val excCallbackDispatcher = Executors.newSingleThreadExecutor().asDispatcher()
val workDispatcher = buildDispatcher { numberOfThreads = numberOfWorkerThreads}
val callDispatcher = buildDispatcher { numberOfThreads = 1 }

val attempts = 10
val warmupRounds = 100000
val timingRounds = 1000000

fun main(args: Array<String>) {
    println(
            """Performance test
- samples:      $attempts
- warmupRounds: $warmupRounds
- timingRounds: $timingRounds
- workers:      $numberOfWorkerThreads""")

    val factors = ArrayList<Double>(attempts)
    for (i in 1..10) {
        configureExecutor()
        validate(warmupRounds)

        val startExc = System.currentTimeMillis()

        validate(timingRounds)
        val deltaExc = System.currentTimeMillis() - startExc

        configureDispatcher()
        validate(warmupRounds)

        val startDis = System.currentTimeMillis()
        validate(timingRounds)
        val deltaDis = System.currentTimeMillis() - startDis

        val factor = deltaExc.toDouble() / deltaDis.toDouble()
        factors add factor
        println("[$i/$attempts] Executor: ${deltaExc}ms, Dispatcher: ${deltaDis}ms. " +
                "Dispatcher is a factor ${factor.format("##0.00")} ${fasterOrSlower(factor)}")
    }

    val averageFactor = factors.sum() / attempts.toDouble()
    println("On average with ${attempts} attempts, "+
            "Dispatcher was a factor ${averageFactor.format("##0.00")} ${fasterOrSlower(averageFactor)}")

    excWorkDispatcher.stop(force = true)
    excCallbackDispatcher.stop(force = true)
}

fun configureExecutor() {
    Kovenant.configure {
        workerDispatcher = excWorkDispatcher
        callbackDispatcher = excCallbackDispatcher
    }
}

fun configureDispatcher() {
    Kovenant.configure {
        workerDispatcher = workDispatcher
        callbackDispatcher = callDispatcher
    }
}


fun validate(n: Int) {
    val promises = Array(n) { n ->
        Kovenant.async {
            val i = 13
            Pair(i, fib(i))
        }
    }

    await(*promises)
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
        p success  { latch.countDown() }
        p fail {latch.countDown() }
    }
    latch.await()
}

private fun Double.format(pattern: String): String = DecimalFormat(pattern).format(this)

private fun fasterOrSlower(value:Double) = if (value < 1.0) "slower" else "faster"