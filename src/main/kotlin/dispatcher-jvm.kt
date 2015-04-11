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

import java.util.ArrayList
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


private class PoolDispatcher(val name: String,
                             val numberOfThreads: Int = availableProcessors,
                             private val exceptionHandler: (Exception) -> Unit = { e -> e.printStackTrace(System.err) },
                             private val errorHandler: (Throwable) -> Unit = { t -> t.printStackTrace(System.err) }) : Dispatcher {

    init {
        if (numberOfThreads < 1) {
            throw IllegalArgumentException("numberOfThreads must be at least 1 but was $numberOfThreads")
        }
    }

    private val running = AtomicBoolean(true)
    private val threadId = AtomicInteger(0)
    private val contextCount = AtomicInteger(0)

    private val threadContexts = ConcurrentLinkedQueue<ThreadContext>()
    private val workQueue = ConcurrentLinkedQueue<() -> Unit>()


    override fun submit(task: () -> Unit): Boolean {
        if (running.get()) {
            workQueue.offer(task)
            val threadSize = contextCount.get()
            if (threadSize < numberOfThreads) {
                val threadNumber = contextCount.incrementAndGet()
                if (threadNumber <= numberOfThreads && threadNumber < workQueue.size()) {
                    val newThreadContext = newThreadContext()
                    threadContexts.offer(newThreadContext)
                    if (!running.get()) {
                        //it can be the case that during initialization of the context the dispatcher has been shutdown
                        //and this newly created created is missed. So request shutdown again.
                        newThreadContext.interrupt()
                    }

                } else {
                    contextCount.decrementAndGet()
                }
            }
            return true
        }

        return false
    }

    override fun shutdown(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> {
        if (running.compareAndSet(true, false)) {

            //Notify all thread to simply die as soon as possible
            threadContexts.forEach { it.kamikaze() }

            if (block) {
                val localTimeOutMs = if (force) 1 else timeOutMs
                val now = System.currentTimeMillis()
                val napTimeMs = Math.min(timeOutMs, 10L)

                fun allThreadsShutdown() = contextCount.get() <= 0
                fun keepWaiting() = localTimeOutMs < 1 || (System.currentTimeMillis() - now) >= localTimeOutMs

                var interrupted = false
                while (!allThreadsShutdown() && keepWaiting()) {
                    try {
                        Thread.sleep(napTimeMs)
                    } catch (e: InterruptedException) {
                        //ignoring for now since it would break the shutdown contract
                        //remember and interrupt later
                        interrupted = true
                    }
                }
                if (interrupted) {
                    //calling thread was interrupted during shutdown, set the interrupted flag again
                    Thread.currentThread().interrupt()
                }

                threadContexts.forEach { it.interrupt() }

                return depleteQueue()
            }
        }
        return ArrayList() //depleteQueue() also returns an ArrayList, returning this for consistency
    }

    override fun isTerminated(): Boolean = isShutdown() && contextCount.get() < 0

    override fun isShutdown(): Boolean = !running.get()

    private fun depleteQueue(): List<() -> Unit> {
        val remains = ArrayList<() -> Unit>()
        do {
            val function = workQueue.poll()
            if (function == null) return remains
            remains.add(function)
        } while (true)
        throw IllegalStateException("unreachable")
    }

    override fun cancel(task: () -> Unit): Boolean = workQueue.remove(task)

    private fun newThreadContext(): ThreadContext {
        return ThreadContext(
                threadName = "${name}-${threadId.incrementAndGet()}",
                waitStrategy = createWaitStrategy())
    }


    internal fun deRegisterRequest(context: ThreadContext, force: Boolean = false): Boolean {

        threadContexts.remove(context)
        if (!force && threadContexts.isEmpty() && workQueue.isNotEmpty() && running.get()) {
            //that, hopefully rare, state where all threadContexts thought they had nothing to do
            //but the queue isn't empty. Reinstate anyone that notes this.
            threadContexts.add(context)
            return false
        }
        contextCount.decrementAndGet()
        return true
    }


    private fun createWaitStrategy(): WaitStrategy {
        return ChainWaitStrategy(BusyPollWaitStrategy(workQueue), SleepPollWaitStrategy(workQueue))
    }

    private inner class ThreadContext(val threadName: String, private val waitStrategy: WaitStrategy) {

        private val pending = 0
        private val running = 1
        private val waiting = 2
        private val mutating = 3

        private val state = AtomicInteger(pending)
        private val thread = Thread() { run() }
        private volatile var alive = true
        private volatile var keepAlive: Boolean = true


        init {
            thread.setName(threadName)
            thread.setDaemon(false)
            thread.start()
        }


        private fun changeState(expect: Int, update: Int) {
            while (state.compareAndSet(expect, update));
        }

        private fun tryChangeState(expect: Int, update: Int) : Boolean = state.compareAndSet(expect, update)

        private fun run() {
            while (alive) {

                val fn = workQueue.poll() // can this throw an InterruptedException?
                if (fn != null) {

                    changeState(pending, running)


                    try {
                        fn()
                    } catch(e: InterruptedException) {
                        if (!alive) {
                            //only set the interrupted flag again if thread is interrupted via this context.
                            //otherwise user code has interrupted the thread, we can ignore that.
                            thread.interrupt()
                        }
                    } catch(e: Exception) {
                        exceptionHandler(e)
                    } catch(t: Throwable) {
                        //Okay this can be anything. Most likely out of memory errors, so everything can go haywire
                        //from here. Let's try to gracefully dismiss this thread by un registering from the pool and die.
                        deRegisterRequest(this, force = true)

                        errorHandler(t)
                    } finally {
                        changeState(running, pending)
                    }

                } else {
                    changeState(pending, waiting)
                    try {
                        if (!keepAlive || !waitStrategy.waitAndReturnAlive()) {
                            //waited and not alive. Who are we to ignore that kind of advice. Let's die.
                            if (deRegisterRequest(this)) {
                                //de register succeeded, shutdown this context.
                                interrupt()
                            }
                        }
                    } catch (e: InterruptedException) {
                        if (!alive) {
                            //only set the interrupted flag again if thread is interrupted via this context.
                            //otherwise either user code has interrupted the thread and we can ignore that
                            //or this thread has become kamikaze and we can ignore that too
                            thread.interrupt()
                        }
                    } finally {
                        changeState(waiting, pending)
                        if (!keepAlive && alive) {
                            //if at this point keepAlive is false but the context itself is alive
                            //the thread might be in an interrupted state, we need to clear that because
                            //we are probably in graceful shutdown mode and we don't want to interfere with any
                            //tasks that need to be executed
                            Thread.interrupted()

                            if (!alive) {
                                //oh you concurrency, you tricky bastard. We might just cleared that interrupted flag
                                //but it can be the case that after checking all the flags this context was interrupted
                                //for a full shutdown and we just cleared that. So interrupt again.
                                thread.interrupt()
                            }
                        }
                    }

                }
            }
        }


        /*
        Become 'kamikaze' or just really suicidal. Try to bypass the waitStrategy for quick and clean death.
         */
        fun kamikaze() {
            keepAlive = false
            if (tryChangeState(waiting, mutating)) {
                //this thread is in the waiting state and we are going to interrupt it.
                //because our waiting strategies might be blocked
                thread.interrupt()
                changeState(mutating, waiting)
            }

        }

        fun interrupt() {
            alive = false
            thread.interrupt()
            deRegisterRequest(this, force = true)
        }
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


private val availableProcessors: Int
    get() = Runtime.getRuntime().availableProcessors()


