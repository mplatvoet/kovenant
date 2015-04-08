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

import java.util.concurrent.*


/**
 * Convenience method to convert an Executor to an Dispatcher
 */
public fun Executor.asDispatcher(): Dispatcher = when (this) {
    is Dispatcher -> this
    is ExecutorService -> ExecutorServiceDispatcher(this)
    else -> ExecutorDispatcher(this)
}

public fun Dispatcher.asExecutor(): Executor = when (this) {
    is Executor -> this
    else -> DispatcherExecutor(this)
}

private data open class ExecutorDispatcher(private val executor: Executor) : Dispatcher, Executor by executor {
    override fun shutdown() {
        /*no op*/
    }

    override fun submit(task: () -> Unit) = executor.execute(task)
}

private data class ExecutorServiceDispatcher(private val executor: ExecutorService) :
        ExecutorDispatcher(executor), ExecutorService by executor

private data open class DispatcherExecutor(private val dispatcher: Dispatcher) : Executor, Dispatcher by dispatcher {
    override fun execute(command: Runnable) = dispatcher.submit { command.run() }
}



