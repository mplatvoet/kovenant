/*
 * Copyright (c) 2016 Mark Platvoet<mplatvoet@gmail.com>
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
package quasar.test

import co.paralleluniverse.strands.Strand
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.quasar.QuasarDispatcher
import nl.komponents.kovenant.task
import java.util.concurrent.CountDownLatch

fun main(args: Array<String>) {
    val dispatcher = QuasarDispatcher("quasar", 1)
    Kovenant.context {
        callbackContext.dispatcher = dispatcher
        workerContext.dispatcher = dispatcher
    }

    val latch = CountDownLatch(20)
    for (i in 1..20) {
        task {
            Strand.sleep(1000)
            println ("awake")
        } fail {
            println(it)
        } always  {
            latch.countDown()
        }
    }
    latch.await()
}

