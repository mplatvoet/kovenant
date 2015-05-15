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

package examples.executors

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.jvm.asExecutorService
import support.fib
import java.util.concurrent.Callable

fun main(args: Array<String>) {
    val executorService = Kovenant.context.workerContext.dispatcher.asExecutorService()

    val tasks = listOf(*(Array(5) { FibCallable(25 - it) }))


    val (n, fib) = executorService invokeAny tasks
    println("invokeAny: fib($n) = $fib")
    println()

    val results = executorService invokeAll tasks
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


