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

package performance.disruptor

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.async
import nl.komponents.kovenant.buildDispatcher
import nl.komponents.kovenant.disruptor.buildDisruptor
import support.fib
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.concurrent.CountDownLatch



val attempts = 10
val warmupRounds = 100000
val timingRounds = 1000000

val numberOfWorkerThreads = Runtime.getRuntime().availableProcessors()
val callDispatcher = buildDispatcher { concurrentTasks = 1 }
val callDisruptor = buildDisruptor { concurrentTasks = 1 }

fun main(args: Array<String>) {
    println(
            """Performance test
- samples:      $attempts
- warmupRounds: $warmupRounds
- timingRounds: $timingRounds
- workers:      $numberOfWorkerThreads""")

    val factors = ArrayList<Double>(attempts)
    for (i in 1..10) {
        configureDisruptor()
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
        println("[$i/$attempts] Disruptor: ${deltaExc}ms, Dispatcher: ${deltaDis}ms. " +
                "Dispatcher is a factor ${fasterOrSlower(factor)}")
    }

    val averageFactor = factors.sum() / attempts.toDouble()
    println("On average with ${attempts} attempts, " +
            "Dispatcher was a factor ${fasterOrSlower(averageFactor)}")

    callDisruptor.stop(force = true)
}

fun configureDisruptor() {
    Kovenant.context {
        callbackContext.dispatcher = callDisruptor
    }
}

fun configureDispatcher() {
    Kovenant.context {
        callbackContext.dispatcher = callDispatcher
    }
}


fun validate(n: Int) {
    val promises = Array(n) { n ->
        async {
            val i = 13
            Pair(i, fib(i))
        }
    }

    await(*promises)
}


private fun await(vararg promises: Promise<*, *>) {
    val latch = CountDownLatch(promises.size())
    promises forEach {
        p ->
        p success  {
            latch.countDown()
        }
        p fail { latch.countDown() }
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