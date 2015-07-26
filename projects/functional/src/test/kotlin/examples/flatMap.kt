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

package examples.flatMap

import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.functional.flatMap

fun main(args: Array<String>) {

    Promise.of(13).flatMap {
        divide(it, 12)
    } success {
        println("Success: $it")
    } fail {
        println("Fail: ${it.getMessage()}")
    }
}

fun divide(a: Int, b: Int): Promise<Int, Exception> {
    return if (a == 0 || b == 0) {
        Promise.ofFail(Exception("Cannot divide by zero: $a/$b"))
    } else {
        Promise.ofSuccess(a / b)
    }
}

