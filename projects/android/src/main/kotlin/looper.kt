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
import nl.mplatvoet.komponents.kovenant.Dispatcher
import nl.mplatvoet.komponents.kovenant.DispatcherContext
import java.util.concurrent.atomic.AtomicInteger


private class LooperExecutor(private val looper: Looper) : Handler.Callback {
    public companion object {
        val main: LooperExecutor = LooperExecutor(Looper.getMainLooper())
    }

    private val untrackedId = Int.MIN_VALUE
    private val handler = Handler(looper, this)
    private val idCounter = AtomicInteger(0)

    override fun handleMessage(msg: Message): Boolean {
        val task = msg.obj as Runnable

        task.run()

        // signal message was handled
        //no need to try other handlers
        return true
    }

    public fun claimTrackingId(): Int {
        var id: Int
        do {
            id = idCounter.incrementAndGet()
        } while (id == untrackedId)
        return id
    }


    public fun tryRemove(trackingId: Int) {
        handler.removeMessages(trackingId)
    }

    public fun submit(runnable: Runnable, trackingId: Int = untrackedId) {
        val message = handler.obtainMessage(trackingId, runnable)
        handler.sendMessage(message)
    }
}