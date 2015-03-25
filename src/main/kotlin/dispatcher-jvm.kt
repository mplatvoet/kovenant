/*
 * Copyright (c) 2014-2015 Mark Platvoet<mplatvoet@gmail.com>
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

package nl.mplatvoet.komponents.kovenant

import java.util.concurrent.Executor
import java.util.concurrent.ConcurrentLinkedQueue





private class ExecutorDispatcher(private val executor: Executor) : Dispatcher {
    override fun submit(task: () -> Unit) {
        executor.execute(task)
    }

    override fun offerHelp(): Boolean = false

}

private class FixedPoolDispatcher(val numberOfThreads: Int = Runtime.getRuntime().availableProcessors()) : Dispatcher {
    init {
        if (numberOfThreads < 1) {
            throw IllegalArgumentException("numberOfThreads must be atleast 1 but was $numberOfThreads")
        }

    }

    private val queue = ConcurrentLinkedQueue<() -> Unit>()


    override fun submit(task: () -> Unit) {
        throw UnsupportedOperationException()
    }

    override fun offerHelp(): Boolean {
        throw UnsupportedOperationException()
    }

}

[suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")]
private class ThreadContext(monitor: Any,
                            private val threadName: String,
                            private val sharedQueue: ConcurrentLinkedQueue<() -> Unit>) {
    private val _monitor = monitor as Object

    private val thread = Thread() { run() };

    init {
        thread.setName(threadName)
        thread.setDaemon(true)
        thread.start()
    }


    private fun run() {
        while (!Thread.currentThread().isInterrupted()) {
            val fn = sharedQueue.poll()
            if (fn != null) {
                fn()
            } else {
                synchronized(_monitor) {
                    //need some nap time
                    try {
                        while (sharedQueue.isEmpty()) _monitor.wait()
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt() // set the interrupted flag again
                    }
                }
            }
        }
    }

    fun interrupt() = thread.interrupt()
}


