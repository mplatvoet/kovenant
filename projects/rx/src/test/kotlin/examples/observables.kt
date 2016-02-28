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

package nl.komponents.kovenant.rx.examples

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.rx.EmitStrategy
import nl.komponents.kovenant.rx.toListPromise
import nl.komponents.kovenant.rx.toPromise
import nl.komponents.kovenant.testMode
import rx.Observable


fun main(args: Array<String>) {
    Kovenant.testMode { }

    //Default behaviour, take first element, exception on empty Observable
    Observable.from(arrayOf(1, 2, 3, 4, 5)).toPromise().printResult()
    Observable.from(arrayOf<Int>()).toPromise().printResult()

    //Take last element
    Observable.from(arrayOf(1, 2, 3, 4, 5)).toPromise(strategy = EmitStrategy.LAST).printResult()

    //default value on empty Observable
    Observable.from(arrayOf<Int>()).toPromise(42).printResult()

    //default value factory on empty Observable
    Observable.from(arrayOf<Int>()).toPromise() { 4 * 16 }.printResult()


    //Put everything in a list
    Observable.from(arrayOf(1, 2, 3, 4, 5)).toListPromise().printResult()
    Observable.from(arrayOf<Int>()).toListPromise().printResult()
}

fun <V, E> Promise<V, E>.printResult() = success {
    println("Success value = $it")
} fail {
    println("Fail value = $it")
}