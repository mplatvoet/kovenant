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

package nl.mplatvoet.komponents.kovenant.android

import nl.mplatvoet.komponents.kovenant.Context
import nl.mplatvoet.komponents.kovenant.ContextAware
import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.Promise

public fun <V, E> Promise<V, E>.successUI(body: (value: V) -> Unit): Promise<V, E> = success {
    LooperExecutor.main.submit(Param1Callback(ctx, it, body))
}

public fun <V, E> Promise<V, E>.failUI(body: (error: E) -> Unit): Promise<V, E> = fail {
    LooperExecutor.main.submit(Param1Callback(ctx, it, body))
}

public fun <V, E> Promise<V, E>.alwaysUI(body: () -> Unit): Promise<V, E> = always {
    LooperExecutor.main.submit(Param0Callback(ctx, body))
}


private class Param1Callback<V>(private val context: Context, private val value: V, private val body: (value: V) -> Unit) : Runnable {
    override fun run() {
        try {
            body(value)
        } catch(e: Exception) {
            context.callbackError(e)
        }
    }
}

private class Param0Callback(private val context: Context, private val body: () -> Unit) : Runnable {
    override fun run() {
        try {
            body()
        } catch(e: Exception) {
            context.callbackError(e)
        }
    }
}


private val <V, E> Promise<V, E>.ctx: Context
    get() = when (this) {
        is ContextAware -> this.context
        else -> Kovenant.context
    }