/*
 * Copyright (c) 2014-2015 Mark Platvoet<mplatvoet@gmail.com>
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package test

import nl.mplatvoet.komponents.kovenant.*
import java.util.Random
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {

    fibonacci()
    executorService()

    validate(10)
    validate(100)
    validate(1000)
}

fun fibonacci() {
    val promises = Array(10) { n ->
        Kovenant.async {
            Pair(n, fib(n))
        } success {
            pair -> println("fib(${pair.first}) = ${pair.second}")
        }
    }

    Kovenant.all(*promises) always {
        println("All promises are done.")
    }
}

private class FibCallable(private val n: Int) :Callable<Int> {
    override fun call(): Int = fib(n)
}

fun executorService() {
    val executorService = Kovenant.context.workerDispatcher.asExecutorService()
    val arrayOfFibCallables = listOf( *(Array(20) {FibCallable(20)}) )


    val result = executorService.invokeAny(arrayOfFibCallables)
    println("invokeAny: fib(20) = ${result}")

//    val results = executorService.invokeAll(arrayOfFibCallables)
//    results forEach {
//        println("invokeAll: fib(20) = ${result}")
//    }
}

fun validate(n:Int) {
    val count = AtomicInteger()
    val successCount = AtomicInteger()
    val promises = Array(n) { n ->
        count.incrementAndGet()
        Kovenant.async {
            val i = Random().nextInt(10)
            Pair(i, fib(i))
        } success {
            count.decrementAndGet()
            successCount.incrementAndGet()
        }
    }

    Kovenant.all(*promises) always {
        println("validate with $n attempts, count: ${count.get()}, successCount: ${successCount.get()}")
    }
}


//a very naive fibonacci implementation
fun fib(n: Int): Int {
    if (n < 0) throw IllegalArgumentException("negative numbers not allowed")
    return when (n) {
        0, 1 -> 1
        else -> fib(n - 1) + fib(n - 2)
    }
}




