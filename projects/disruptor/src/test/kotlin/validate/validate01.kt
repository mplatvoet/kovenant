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

package validate.disruptor

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.all
import nl.mplatvoet.komponents.kovenant.async
import nl.mplatvoet.komponents.kovenant.disruptor.DisruptorDispatcher
import support.fib
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    val disruptorDispatcher = DisruptorDispatcher()
    Kovenant.configure {
        callbackDispatcher = disruptorDispatcher
    }
    validate(10000000)

    disruptorDispatcher.stop()
}


fun validate(n: Int) {
    val errors = AtomicInteger()
    val successes = AtomicInteger()
    val promises = Array(n) { n ->
        errors.incrementAndGet()
        async {
            val i = Random().nextInt(10)
            Pair(i, fib(i))
        } success {
            errors.decrementAndGet()
            successes.incrementAndGet()
        }
    }

    all(*promises) always {
        println("validate with $n attempts, errors: ${errors.get()}, successes: ${successes.get()}")
    }

}