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
import support.fib

fun main(args: Array<String>) {
    Kovenant.context {
        workerContext.dispatcher { concurrentTasks = 8 }
    }

    val sleepGate = Gate(2)

    (1..10).forEach { number ->
        sleepGate.async {
            //mimic work by sleeping
            Thread.sleep(1000)
            number
        }.success {
            println("$number is done")
        }
    }

    val fibGate = Gate(4)
    (1..10).forEach { number ->
        fibGate.async {
            val n = 35 + number
            Pair(n, fib(n))
        } .success {
            val (n, fib) = it
            println("fib($n) = $fib")
        }
    }

    println("all tasks created")
}
