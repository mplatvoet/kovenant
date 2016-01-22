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
@file:JvmName("KovenantContextApi")

package nl.komponents.kovenant


public object Kovenant {
    private val concrete = ConcreteKovenant()

    public var context: Context
        get() = concrete.context
        set(value) {
            concrete.context = value
        }


    public fun context(body: MutableContext.() -> Unit): Context = concrete.context(body)

    public fun createContext(body: MutableContext.() -> Unit): Context = concrete.createContext(body)

    public fun <V, E> deferred(context: Context = Kovenant.context): Deferred<V, E> = concrete.deferred(context)

    fun stop(force: Boolean = false, timeOutMs: Long = 0, block: Boolean = true): List<() -> Unit> {
        return context.stop(force, timeOutMs, block)
    }

}

public interface Context {
    val multipleCompletion: (curVal: Any?, newVal: Any?) -> Unit

    val callbackContext: DispatcherContext
    val workerContext: DispatcherContext

    fun stop(force: Boolean = false, timeOutMs: Long = 0, block: Boolean = true): List<() -> Unit> {
        val callbackTasks = callbackContext.dispatcher.stop(force, timeOutMs, block)
        val workerTasks = workerContext.dispatcher.stop(force, timeOutMs, block)
        return callbackTasks + workerTasks
    }
}

public interface MutableContext : Context {
    override val callbackContext: MutableDispatcherContext
    override val workerContext: MutableDispatcherContext

    override var multipleCompletion: (curVal: Any?, newVal: Any?) -> Unit

    fun callbackContext(body: MutableDispatcherContext.() -> Unit) {
        callbackContext.body()
    }

    fun workerContext(body: MutableDispatcherContext.() -> Unit) {
        workerContext.body()
    }


}

public interface ReconfigurableContext : MutableContext {
    fun copy(): ReconfigurableContext
}

public interface DispatcherContext {
    companion object {
        public fun create(dispatcher: Dispatcher,
                          errorHandler: (Exception) -> Unit): DispatcherContext
                = StaticDispatcherContext(dispatcher, errorHandler)
    }

    val dispatcher: Dispatcher
    val errorHandler: (Exception) -> Unit

    public fun offer(fn: () -> Unit): Unit {
        try {
            dispatcher.offer(fn)
        } catch (e: Exception) {
            errorHandler(e)
        }
    }
}

/* Use a DirectDispatcherContext to avoid scheduling. */
object DirectDispatcherContext : DispatcherContext {
    val errorFn: (Exception) -> Unit = { e -> e.printStackTrace() }

    override val dispatcher: Dispatcher = DirectDispatcher.instance
    override val errorHandler: (Exception) -> Unit get() = errorFn
}

public interface MutableDispatcherContext : DispatcherContext {
    override var dispatcher: Dispatcher
    override var errorHandler: (Exception) -> Unit

    fun dispatcher(body: DispatcherBuilder.() -> Unit) {
        dispatcher = buildDispatcher(body)
    }
}

private class StaticDispatcherContext(override val dispatcher: Dispatcher,
                                      override val errorHandler: (Exception) -> Unit) : DispatcherContext


public fun Kovenant.testMode(failures: (Throwable) -> Unit = { throw it }) {
    Kovenant.context {
        callbackContext.dispatcher = DirectDispatcher.instance
        callbackContext.errorHandler = failures

        workerContext.dispatcher = DirectDispatcher.instance
        workerContext.errorHandler = failures

        multipleCompletion = {
            first, second -> failures(KovenantException("multiple completion: first = $first, second = $second"))
        }
    }
}
