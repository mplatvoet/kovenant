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


/**
 * a Dispatcher 'executes' a task somewhere in the future. Yes, I'd rather called this an Executor but
 * that is way to similar to java's Executors. This way it's more obvious to distinct the two types. The reason why
 * I don't use java's Executor directly is because it pollutes the API, I want to keep this platform agnostic.
 */
trait Dispatcher {

    /**
     *
     * @param task the task to be executed by this dispatcher
     * @return true if the task was scheduled, false if this dispatcher has shutdown or is shutting down
     */
    fun submit(task: () -> Unit) : Boolean

    /**
     * Shuts down this dispatcher therefor stops accepting new tasks. This methods blocks and executes everything that
     * is still queued unless force or timeOutMs is used. Thus by default this method returns an empty list.
     * Any subsequent (concurrent) calls to this function will be ignored and just returns an empty list.
     *
     * @param force forces shutdown by cancelling all running tasks and killing threads as soon as possible
     * @param timeOutMs for every timeOutMs > 0 the dispatcher tries to shutdown gracefully. Meaning flushing the queue
     *                  until the timeOutMs is reached and then forcing shutdown.
     * @param block blocks until done if true, returns with an empty list otherwise
     *
     * @return tasks that where not yet started, does not include cancelled tasks
     */
    fun shutdown(force: Boolean = false, timeOutMs: Long = 0, block: Boolean = true): List<() -> Unit>

    /**
     * Cancels a previously scheduled task, Does, of course, not execute the provided task.
     * Note that this method cancels tasks by means of equality, so be careful when using method
     * references which effectively creates multiple Function instances and are there not equal.
     *
     * @return true if the task was cancelled, false otherwise
     */
    fun cancel(task: ()-> Unit) : Boolean


    /**
     * @return true if dispatcher is shutdown all threads have been shutdown, false otherwise
     */
    fun isTerminated() : Boolean

    /**
     * @return true if shutdown has been invoked, false otherwise.
     */
    fun isShutdown() : Boolean
}

public class RejectedException(msg: String, val task: () -> Unit) : Exception(msg)


