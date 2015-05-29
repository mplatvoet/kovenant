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

    val context: Context
        get() = concrete.context

    public fun configure(body: MutableContext.() -> Unit): Unit = concrete.configure(body)

    public fun createContext(body: MutableContext.() -> Unit): Context = concrete.createContext(body)

    public fun deferred<V, E>(context: Context = Kovenant.context): Deferred<V, E> = concrete.deferred(context)

}

public interface Context {
    val multipleCompletion: (curVal: Any, newVal: Any) -> Unit

    val callbackContext: DispatcherContext
    val workerContext: DispatcherContext
}

public interface MutableContext : Context {
    var callbackDispatcher: Dispatcher
    var workerDispatcher: Dispatcher
    var callbackError: (Exception) -> Unit
    var workerError: (Exception) -> Unit
    override var multipleCompletion: (curVal: Any, newVal: Any) -> Unit
}

public interface DispatcherContext {
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

//public trait MutableDispatcherContext : DispatcherContext {
//    override var dispatcher: Dispatcher
//    override var errorHandler: (Exception) -> Unit
//}



