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

package nl.komponents.kovenant


public object Kovenant {
    private val concrete = ConcreteKovenant()

    public var context: Context
        get() = concrete.context
        set(value) {
            concrete.context = value
        }


    public fun context(body: MutableContext.() -> Unit): Context = concrete.context(body)

    @Deprecated("use context { ... } instead", ReplaceWith("context(body)"))
    public fun configure(body: MutableContext.() -> Unit): Unit {
        context(body)
    }

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

    @Deprecated("use callbackContext.dispatcher instead", ReplaceWith("callbackContext.dispatcher"))
    val callbackDispatcher: Dispatcher get() = callbackContext.dispatcher

    @Deprecated("use workerContext.dispatcher instead", ReplaceWith("workerContext.dispatcher"))
    val workerDispatcher: Dispatcher get() = workerContext.dispatcher

    @Deprecated("use callbackContext.errorHandler instead", ReplaceWith("callbackContext.errorHandler"))
    val callbackError: (Exception) -> Unit get() = callbackContext.errorHandler

    @Deprecated("use workerContext.errorHandler instead", ReplaceWith("workerContext.errorHandler"))
    val workerError: (Exception) -> Unit get() = workerContext.errorHandler
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

    @Deprecated("use callbackContext.dispatcher instead", ReplaceWith("callbackContext.dispatcher"))
    override var callbackDispatcher: Dispatcher
        get() = callbackContext.dispatcher
        set(value) {
            callbackContext.dispatcher = value
        }

    @Deprecated("use workerContext.dispatcher instead", ReplaceWith("workerContext.dispatcher"))
    override var workerDispatcher: Dispatcher
        get() = workerContext.dispatcher
        set(value) {
            workerContext.dispatcher = value
        }

    @Deprecated("use callbackContext.errorHandler instead", ReplaceWith("callbackContext.errorHandler"))
    override var callbackError: (Exception) -> Unit
        get() = callbackContext.errorHandler
        set(value) {
            callbackContext.errorHandler = value
        }

    @Deprecated("use workerContext.errorHandler instead", ReplaceWith("workerContext.errorHandler"))
    override var workerError: (Exception) -> Unit
        get() = workerContext.errorHandler
        set(value) {
            workerContext.errorHandler = value
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



