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

package nl.komponents.kovenant.disruptor.queue

import com.lmax.disruptor.EventFactory
import com.lmax.disruptor.RingBuffer
import nl.komponents.kovenant.BlockingSupportWorkQueue
import nl.komponents.kovenant.WorkQueue
import com.lmax.disruptor.Sequence as Seq


public fun disruptorWorkQueue(capacity: Int = 1024): WorkQueue<() -> Unit> {
    return DisruptorWorkQueue({ -> Unit }, capacity)
}

private class DisruptorWorkQueue<V : Any>(initialValue: V, capacity: Int) : BlockingSupportWorkQueue<V>() {

    private val buffer = RingBuffer.createMultiProducer(ContainerFactory(initialValue), PowerOfTwo.roundUp(capacity))
    private val seq = Seq()

    init {
        buffer.addGatingSequences(seq)
        seq.set(buffer.getCursor())
    }

    public override fun size(): Int {
        // tail **needs** to be retrieved before head
        // because in highly concurrent scenario's if retrieved
        // the other way around, the tail might be beyond the head
        // so don't change this to a one-liner
        val tail = seq.get()
        val head = buffer.getCursor()
        return (head - tail).toInt()
    }

    //Can't really ensure atomicity on this. So report false for now.
    public override fun remove(elem: Any?): Boolean {
        //        var idx = buffer.getCursor()
        //        while(idx > seq.get()) {
        //            val tail = seq.get()
        //            val container = buffer[idx]
        //            val value = container.value
        //            if (value == elem
        //                    && buffer.isPublished(idx)
        //                    && container.reset(value)) {
        //                return true
        //            }
        //            --idx
        //        }
        return false
    }


    override fun tryOffer(elem: V): Boolean {
        val idx = buffer.next()
        try {
            buffer[idx].value = elem
        } finally {
            buffer.publish(idx)
        }
        return true
    }


    override fun tryPoll(): V? {
        while (true) {
            // Determine if the next sequence is a published
            // one. Otherwise this queue can be considered
            // being empty
            val probeIdx = seq.get() + 1
            if (buffer.isPublished(probeIdx)) {

                // There is a published item try to get exclusive
                // right on this slot by comparing and setting
                // multiple threads might be fighting for this
                val value = buffer[probeIdx].value
                if (seq.compareAndSet(probeIdx - 1, probeIdx)) {
                    return value
                }
            } else {
                return null
            }
        }
    }
}


private class Container<V>(private val initialValue: V) {
    public var value: V = initialValue

    /*for deletion*/
    //    private val valueRef = AtomicReference(initialValue)
    //    public var value: V
    //        get() = valueRef.get()
    //        set(newValue) {
    //            valueRef.set(newValue)
    //        }
    //
    //    public fun reset(expected: V) : Boolean = valueRef.compareAndSet(expected, initialValue)

}

private class ContainerFactory<V>(private val initialValue: V) : EventFactory<Container<V>> {
    override fun newInstance(): Container<V> = Container(initialValue)
}


private object PowerOfTwo {
    private val max = 1 shl 30

    public fun roundUp(value: Int): Int = when {
        value >= max -> max
        value > 1 -> Integer.highestOneBit((value - 1) shl 1)
        else -> 1
    }
}