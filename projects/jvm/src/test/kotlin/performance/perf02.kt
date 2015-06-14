/*
 * Copyright (c) 2015 Mark Platvoet<mplatvoet@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * THE SOFTWARE.
 */

package performance.perf02

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.async
import nl.komponents.kovenant.buildDispatcher
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

val cbDispatch = buildDispatcher {
    concurrentTasks = 1
    pollStrategy {
        yielding()
        blocking()
    }
}
val workDispatch = buildDispatcher {
    pollStrategy {
        yielding()
        blocking()
    }
}

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
        callbackContext.dispatcher = cbDispatch
        workerContext.dispatcher = workDispatch
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
                "Promises are a factor ${fasterOrSlower(factor)}")
        napTime()

    }

    val averageFactor = factors.sum() / attempts.toDouble()
    println("On average with ${attempts} attempts, " +
            "Promises where a factor ${fasterOrSlower(averageFactor)}")

    executorService.shutdownNow()
    workDispatch.stop()
    cbDispatch.stop()
}

private fun napTime() {
    if (napTimeSeconds > 0) {
        System.gc()
        Thread.sleep(TimeUnit.MILLISECONDS.convert(napTimeSeconds, TimeUnit.SECONDS))
    }
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

private fun fasterOrSlower(value: Double): String {
    if (value == 0.0) {
        return "<undetermined>"
    }
    if (value < 1.0) {

        return "${(1.0 / value).format("##0.00")} slower"
    }
    return "${value.format("##0.00")} faster"
}