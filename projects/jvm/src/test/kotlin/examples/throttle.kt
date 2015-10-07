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

package examples.throttle

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.jvm.Throttle
import nl.komponents.kovenant.then

fun main(args: Array<String>) {
    Kovenant.context {
        workerContext.dispatcher { concurrentTasks = 8 }
    }

    val sleepThrottle = Throttle(4)
    val snoozeThrottle = Throttle(2)

    val promises = (1..10).map { number ->
        sleepThrottle.task {
            Thread.sleep(1000)
            number
        } success {
            println("#$it sleeper awakes")
        }
    }

    promises.forEach { promise ->
        val registeredPromise = snoozeThrottle.registerTask(promise) {
            Thread.sleep(700)
        }

        val finalPromise = registeredPromise then {
            "#${promise.get()} snoozing"
        }

        snoozeThrottle.registerDone(finalPromise)

        finalPromise success {
            println(it)
        }

    }

    println("all tasks created")
}