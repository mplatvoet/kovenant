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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package incubating.gate

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.incubating.Gate


fun main(args: Array<String>) {
    Kovenant.context {
        workerContext.dispatcher { concurrentTasks = 8 }
    }

    val sleepGate = Gate(4)
    val snoozeGate = Gate(2)

    val promises = (1..10).map { number ->
        sleepGate.async {
            Thread.sleep(1000)
            number
        } success {
            println("${delta}ms, #$it sleeper awakes")
        }
    }

    promises.forEach { promise ->
        snoozeGate.then(promise) { Thread.sleep(700) } success {
            println("${delta}ms, #${promise.get()} snoozer bleeped")
        }
    }

    println("all tasks created")
}

val start = System.currentTimeMillis()
val delta: Long  get() = System.currentTimeMillis() - start

