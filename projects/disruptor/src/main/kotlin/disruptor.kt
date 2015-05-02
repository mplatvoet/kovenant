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

package nl.mplatvoet.komponents.kovenant.disruptor

import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.EventHandler
import com.lmax.disruptor.SleepingWaitStrategy
import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor.dsl.ProducerType
import nl.mplatvoet.komponents.kovenant.Dispatcher
import java.util.concurrent.Executors

//TODO provide kotlin configuration
class DisruptorDispatcher() : Dispatcher {
    private val disrupter = Disruptor(FunctionEventFactory(), 1024, Executors.newFixedThreadPool(1), ProducerType.MULTI, SleepingWaitStrategy())
    private val buffer = disrupter.getRingBuffer()

    init {
        disrupter.handleEventsWith(FunctionEventHandler())
        //TODO error handling

        disrupter.start()
    }

    override fun offer(task: () -> Unit): Boolean {
        val seq = buffer.next()
        try {
            buffer[seq].value = task
        } finally {
            buffer.publish(seq)
        }
        return true
    }


    //TODO, implement this properly
    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        //disrupter.shutdown() // shutdown only works properly if publishing has stopped.
        //needs some guards here
        return listOf()
    }

    override fun tryCancel(task: () -> Unit): Boolean {
        throw UnsupportedOperationException()
    }

    override val terminated: Boolean
        get() = throw UnsupportedOperationException()
    override val stopped: Boolean
        get() = throw UnsupportedOperationException()


}

private class FunctionEvent() {
    public var value: () -> Unit = {}
}

private class FunctionEventHandler : EventHandler<FunctionEvent> {
    override fun onEvent(event: FunctionEvent, sequence: Long, endOfBatch: Boolean) {
        event.value()
    }

}

private class FunctionEventFactory : EventFactory<FunctionEvent> {
    override fun newInstance(): FunctionEvent = FunctionEvent()
}

