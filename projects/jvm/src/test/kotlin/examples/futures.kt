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

package examples.futures

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.async
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.jvm.asDispatcher
import support.fib
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool

fun main(args: Array<String>) {
    Kovenant.context {
        workerContext.dispatcher = ForkJoinPool.commonPool().asDispatcher()
    }


    val completableFuture: CompletableFuture<Int> = CompletableFuture.supplyAsync {
        fib(13)
    }

    val promise = completableFuture.toPromise()

    promise success {
        println("Hurray! fib(13) = $it")
    }

    async { fib(13) } success {
        println("Hurray again! fib(13) = $it")
    }
}

public fun <T : Any> CompletableFuture<T>.toPromise(): Promise<T, Exception> {
    val deferred = deferred<T, Exception>()

    thenAccept {
        deferred.resolve(it)
    }

    exceptionally {
        deferred.reject(it.toException())
        //TODO, works but might be a better way
        null as T
    }

    return deferred.promise
}

private fun Throwable.toException(): Exception = when (this) {
    is Exception -> this
    else -> Exception(this)
}

