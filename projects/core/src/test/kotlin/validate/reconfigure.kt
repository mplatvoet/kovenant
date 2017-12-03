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

package validate.reconfigure

import nl.komponents.kovenant.*
import support.fib
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    validate(10000)
}


fun validate(n:Int) {
    val errors = AtomicInteger()
    val successes = AtomicInteger()

    val firstBatch = Array(n) { _ ->
        errors.incrementAndGet()
        task {
            val i = Random().nextInt(10)
            Pair(i, fib(i))
        } success {
            errors.decrementAndGet()
            successes.incrementAndGet()
        }
    }

    Kovenant.context {
        workerContext.dispatcher = buildDispatcher { concurrentTasks = 2 }
        callbackContext.dispatcher = buildDispatcher { concurrentTasks = 1 }
    }

    val secondBatch = Array(n) { _ ->
        errors.incrementAndGet()
        task {
            val i = Random().nextInt(10)
            Pair(i, fib(i))
        } success {
            errors.decrementAndGet()
            successes.incrementAndGet()
        }
    }


    val promises = arrayOf(*firstBatch, *secondBatch)

    all(*promises) always {
        println("validate with ${n*2} attempts, errors: ${errors.get()}, successes: ${successes.get()}")
    }
}
