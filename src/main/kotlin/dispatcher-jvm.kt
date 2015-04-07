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

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger


/**
 * Convenience method to convert an Executor to an Dispatcher
 */
public fun Executor.asDispatcher(): Dispatcher = ExecutorDispatcher(this)

private data class ExecutorDispatcher(private val executor: Executor) : Dispatcher {
    override fun submit(task: () -> Unit) = executor.execute(task)
}

private class PoolDispatcher(val name: String, val numberOfThreads: Int = availableProcessors) : Dispatcher {

    init {
        if (numberOfThreads < 1) {
            throw IllegalArgumentException("numberOfThreads must be at least 1 but was $numberOfThreads")
        }
    }

    private volatile var running = true
    private val threadId = AtomicInteger(0)
    private val contextCount = AtomicInteger(0)

    private val threadContexts = ConcurrentLinkedQueue<ThreadContext>()
    private val workQueue = ConcurrentLinkedQueue<() -> Unit>()


    override fun submit(task: () -> Unit) {
        if (running) {
            workQueue.offer(task)
            val threadSize = contextCount.get()
            if (threadSize < numberOfThreads) {
                val threadNumber = contextCount.incrementAndGet()
                if (threadNumber <= numberOfThreads && threadNumber < workQueue.size()) {
                    threadContexts.offer(ThreadContext(this, createWaitStrategy(), workQueue, "${name}-${threadId.incrementAndGet()}"))

                } else {
                    contextCount.decrementAndGet()
                }
            }
        } else {
            //TODO, how to handle rejections?
        }
    }


    internal fun deRegisterRequest(context: ThreadContext, force: Boolean = false): Boolean {

        threadContexts.remove(context)
        if (!force && threadContexts.isEmpty() && workQueue.isNotEmpty() && running) {
            //that, hopefully rare, state where all threadContexts thought they had nothing to do
            //but the queue isn't empty. Reinstate anyone that notes this.
            threadContexts.add(context)
            return false
        }
        contextCount.decrementAndGet()
        return true
    }

    public fun shutdown() {
        //TODO doesn't prevent from threads being created during shutdown and thus missed
        running = false
        threadContexts.forEach { it.interrupt() }
    }

    private fun createWaitStrategy(): WaitStrategy {
        return ChainWaitStrategy(BusyPollWaitStrategy(workQueue), SleepPollWaitStrategy(workQueue))
    }


}

private trait WaitStrategy {
    /*
    Waits for any amount of time determined by the strategy used
    Returns true if thread should expect more work, false if it can be shutdown
     */
    fun waitAndReturnAlive(): Boolean
}

private class ChainWaitStrategy(private vararg val strategies: WaitStrategy) : WaitStrategy {
    override fun waitAndReturnAlive(): Boolean {
        strategies.forEach {
            if (it.waitAndReturnAlive()) return true
        }
        return false
    }
}

private class BusyPollWaitStrategy(private val queue: ConcurrentLinkedQueue<*>,
                                   private val attempts: Int = 1000) : WaitStrategy {
    override fun waitAndReturnAlive(): Boolean {
        for (i in 0..attempts) {
            if (queue.isNotEmpty()) return true
            Thread.yield()
        }
        return false
    }
}

private class SleepPollWaitStrategy(private val queue: ConcurrentLinkedQueue<*>,
                                    private val attempts: Int = 100,
                                    private val sleepTimeMs: Long = 10) : WaitStrategy {
    override fun waitAndReturnAlive(): Boolean {
        for (i in 0..attempts) {
            if (queue.isNotEmpty()) return true
            Thread.sleep(sleepTimeMs)
        }
        return false
    }
}


private class ThreadContext(private val poolDispatcher: PoolDispatcher,
                            private val waitStrategy: WaitStrategy,
                            private val sharedQueue: ConcurrentLinkedQueue<() -> Unit>,
                            val threadName: String
) {
    private val thread = Thread() { run() }
    private volatile var running = true

    init {
        thread.setName(threadName)
        thread.setDaemon(false)
        thread.start()
    }


    private fun run() {
        while (running) {

            val fn = sharedQueue.poll() // can this throw an InterruptedException?
            if (fn != null) {
                try {
                    fn()
                } catch(e: InterruptedException) {
                    if (!running) {
                        //only set the interrupted flag again if thread is interrupted via this context.
                        //otherwise user code has interrupted the thread, we can ignore that.
                        thread.interrupt()
                    }
                } catch(e: Exception) {
                    //this is pure evil, ignoring exceptions for now. Should use some global logger so this
                    //doesn't get lost.
                    //TODO: handle exception
                } catch(t: Throwable) {
                    //Okay this can be anything. Most likely out of memory errors, so everything can go haywire
                    //from here. Let's try to gracefully dismiss this thread by un registering from the pool and die.
                    poolDispatcher.deRegisterRequest(this, force = true)

                    throw t
                }

            } else {
                try {
                    if (!waitStrategy.waitAndReturnAlive()) {
                        //waited and not alive. Who are we to ignore that kind of advice. Let's die.
                        if (poolDispatcher.deRegisterRequest(this)) {
                            //de register succeeded, shutdown this context.
                            interrupt()
                        }
                    }
                } catch (e: InterruptedException) {
                    if (!running) {
                        //only set the interrupted flag again if thread is interrupted via this context.
                        //otherwise user code has interrupted the thread, we can ignore that.
                        thread.interrupt()
                    }
                }

            }
        }
    }

    fun interrupt() {
        running = false
        thread.interrupt()
    }
}


private val availableProcessors: Int
    get() = Runtime.getRuntime().availableProcessors()


