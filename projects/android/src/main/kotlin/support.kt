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

import android.os.Handler
import android.os.Looper
import android.os.Message
import nl.mplatvoet.komponents.kovenant.*
import java.util.concurrent.atomic.AtomicInteger

public fun <V, E> Promise<V, E>.successUI(body: (value: V) -> Unit): Promise<V, E> = success {
    UIThreadRunner.submit(Param1Callback(ctx, it, body))
}

public fun <V, E> Promise<V, E>.failUI(body: (error: E) -> Unit): Promise<V, E> = fail {
    UIThreadRunner.submit(Param1Callback(ctx, it, body))
}

public fun <V, E> Promise<V, E>.alwaysUI(body: () -> Unit): Promise<V, E> = always {
    UIThreadRunner.submit(Param0Callback(ctx, body))
}

public fun androidUIDispatcher(): Dispatcher = AndroidUIDispatcher()

private trait Task {
    fun execute()
}

private class Param1Callback<V>(private val context: Context, private val value: V, private val body: (value: V) -> Unit) : Task {
    override fun execute() {
        try {
            body(value)
        } catch(e: Exception) {
            context.callbackError(e)
        }
    }
}

private class Param0Callback(private val context: Context, private val body: () -> Unit) : Task {
    override fun execute() {
        try {
            body()
        } catch(e: Exception) {
            context.callbackError(e)
        }
    }
}

private class Work(private val body: () -> Unit) : Task {
    override fun execute() = body()
}

private class AndroidUIDispatcher() : Dispatcher {
    private volatile var running = true
    override fun offer(task: () -> Unit): Boolean {
        if (running) {

            UIThreadRunner.submit(Work(task))

            return true
        }
        return false
    }

    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        running = false

        // TODO So, what to do with stopping. We could track our work but that seriously reduces throughput
        return listOf()
    }

    //TODO determine whether to implement this. Seriously reduces throughput
    override fun tryCancel(task: () -> Unit): Boolean = false

    override val terminated: Boolean
        get() = false
    override val stopped: Boolean
        get() = running

}

private object UIThreadRunner : Handler.Callback {
    private val handler = Handler(Looper.getMainLooper())
    private val idCounter = AtomicInteger(0)

    override fun handleMessage(msg: Message): Boolean {
        val task = msg.obj as Task
        try {
            task.execute()
        } finally {
            //not necessary but let's clean quick and swift
            msg.recycle()
        }
        // signal message was handled
        //no need to try other handlers
        return true
    }

    public fun cancel(id: Int) {
        handler.removeMessages(id)
    }

    //TODO idCounter is introduced to be able to support cancelling
    //maybe not that important to dispatcher, but a different game
    //when a dispatcher is converted to an Executor. Things should
    //work as expected, even if it doesn't make sense of doing.
    public fun submit(callback: Task): Int {
        val id = idCounter.incrementAndGet()
        val message = handler.obtainMessage(id, callback)
        handler.dispatchMessage(message)
        return id
    }
}


private val <V, E> Promise<V, E>.ctx: Context
    get() = when (this) {
        is ContextAware -> this.context
        else -> Kovenant.context
    }