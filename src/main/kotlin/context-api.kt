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


public object Kovenant {
    private val concrete = ConcreteKovenant()

    val context: Context = concrete.context

    public fun configure(body: MutableContext.() -> Unit) : Unit = concrete.configure(body)
}

public trait Context {
    val callbackDispatcher: Dispatcher
    val workerDispatcher: Dispatcher
    val callbackError: (Exception) -> Unit
    val multipleCompletion: (curVal: Any, newVal: Any) -> Unit
}

public trait MutableContext : Context {
    override var callbackDispatcher: Dispatcher
    override var workerDispatcher: Dispatcher
    override var callbackError: (Exception) -> Unit
    override var multipleCompletion: (curVal: Any, newVal: Any) -> Unit
}