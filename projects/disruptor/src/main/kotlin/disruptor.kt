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

import com.lmax.disruptor.*
import com.lmax.disruptor.dsl.Disruptor
import com.lmax.disruptor.dsl.ProducerType
import nl.mplatvoet.komponents.kovenant.Dispatcher
import java.util.ArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

public fun buildDisruptor(body: DisruptorBuilder.() -> Unit): Dispatcher {
    val builder = ConcreteDisruptorBuilder()
    builder.body()
    return builder.buildDispatcher()
}

public enum class Producers {
    MULTIPLE SINGLE
}

public trait DisruptorBuilder {
    var name: String
    var numberOfThreads: Int
    var bufferSize: Int
    var producers: Producers
}

private class ConcreteDisruptorBuilder : DisruptorBuilder {
    private var threads = 1
    private var producerType = ProducerType.MULTI
    private var buffer = 1024

    override var name = "kovenant-disruptor"
    override var numberOfThreads: Int
        get() = threads
        set(value) {
            if (value < 1) throw IllegalArgumentException("can't have less then one thread")
            threads = value
        }

    override var bufferSize: Int
        get() = buffer
        set(value) {
            if (value < 0) throw IllegalArgumentException("negative buffer size, really?")
            buffer = PowerOfTwo.roundUp(value)
        }
    override var producers: Producers
        get() = when (producerType) {
            ProducerType.MULTI -> Producers.MULTIPLE
            ProducerType.SINGLE -> Producers.SINGLE
        }

        set(value) {
            producerType = when (value) {
                Producers.MULTIPLE -> ProducerType.MULTI
                Producers.SINGLE -> ProducerType.SINGLE
            }
        }

    fun buildDispatcher(): Dispatcher {
        return SingleThreadDisruptorDispatcher()
        //return DisruptorDispatcher(name, buffer, threads, producerType)
    }
}


private class DisruptorDispatcher(private val name: String,
                                  private val bufferSize: Int,
                                  private val threads: Int,
                                  private val producerType: ProducerType) : Dispatcher {


    val executorService = Executors.newFixedThreadPool(threads) {
        runnable ->
        val thread = Thread(runnable, name)
        thread.setDaemon(false)
        thread
    }

    private val disrupter = Disruptor(FunctionEventFactory(),
            bufferSize,
            executorService,
            producerType,
            SleepingWaitStrategy())

    private val buffer = disrupter.getRingBuffer()

    private volatile var running = true
    private val publishers = AtomicInteger(0)

    init {
        disrupter.handleEventsWith(FunctionEventHandler())
        //TODO error handling

        disrupter.start()
    }

    override fun offer(task: () -> Unit): Boolean {
        if (!running) return false;
        publishers.incrementAndGet()
        try {
            if (!running) return false;

            publishToRingBuffer(task)
        } finally {
            publishers.decrementAndGet()
        }
        return true
    }

    private fun publishToRingBuffer(task: () -> Unit) {

        val seq = buffer.next()
        try {
            buffer[seq].value = task
        } finally {
            buffer.publish(seq)
        }
    }


    //TODO, implement this properly
    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        //        running = false
        //        while (publishers.get() > 0) {
        //            //just spin till all the publishers are gone
        //        }
        //        disrupter.shutdown()
        //        executorService.shutdown()
        //        executorService.awaitTermination(1, TimeUnit.DAYS)
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


private class SingleThreadDisruptorDispatcher() : Dispatcher {
    private val buffer = RingBuffer.createMultiProducer({ FunctionEvent() }, PowerOfTwo.roundUp(1000000), BlockingWaitStrategy())

    private val barrier = buffer.newBarrier()
    private val thread = Thread({ dispatch() }, "disruptor")

    private volatile var running = true

    init {
        thread.setDaemon(false)
        thread.start()
    }

    override fun offer(task: () -> Unit): Boolean {
        //TODO if buffer is too small, buffer.next goes beyond read point and makes the whole Disruptor
        //TODO deadlock. How does blocking work? I probably poorly configured the Disruptor.

        val seq = buffer.next()
        try {
            buffer[seq].value = task
        } finally {
            buffer.publish(seq)
        }
        return true
    }


    //only call from dispatcher thread
    private fun dispatch() {
        val callables = ArrayList<() -> Unit>()
        var seq = 0L
        while (running) {
            try {
                val nextSeq = barrier.waitFor(seq)
                val batchSize = (nextSeq - seq).toInt() + 1
                if (batchSize > 0) {
                    callables.ensureCapacity(batchSize)
                    callables.clear()
                    while (seq <= nextSeq) {
                        callables add buffer[seq].value

                        ++seq
                    }

                    callables.executeAll()
                    callables.clear()
                }
                //println("seq: $seq, nextSeq: $nextSeq")
            } catch(e: Exception) {
                e.printStackTrace(System.err)
            }
        }
    }

    //only call from dispatcher thread
    private fun ArrayList<() -> Unit>.executeAll() {
        forEach {
            fn ->
            fn.invoke()
        }
    }

    //TODO, implement this properly
    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        running = false

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

private object PowerOfTwo {
    private val max = 1 shl 30

    public fun roundUp(value: Int): Int = when {
        value >= max -> max
        value > 1 -> Integer.highestOneBit((value - 1) shl 1)
        else -> 1
    }

}

