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
package nl.mplatvoet.kotlin.komponents.promises

private class DeferredPromise<V, E>(private val config: Context) : JvmCallbackSupport<V, E>(), Promise<V, E>, ResultVisitor<V, E>, Deferred<V, E> {


    override fun resolve(value: V) = setResult(ValueResult(value))
    override fun reject(error: E) = setResult(ErrorResult(error))

    override val promise: Promise<V, E> = this

    private fun setResult(result: Result<V, E>) {
        if (trySetResult(result)) {
            result.accept(this)
            fire (alwaysCbs)
        } else {
            config.multipleCompletion(this.result.rawValue, result.rawValue)
        }
    }

    override fun visitValue(value: V) = fire(successCbs, value)
    override fun visitError(error: E) = fire(failCbs, error)

    override fun success(callback: (value: V) -> Unit): Promise<V, E> {
        val res = result
        if (res != null) {
            if (res is ValueResult) config.tryDispatch { callback(res.value) }
        } else {
            addSuccessCb(callback)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            val res2 = result
            if (res2 != null && res2 is ValueResult) fire (successCbs, res2.value)
        }

        return this
    }

    override fun fail(callback: (error: E) -> Unit): Promise<V, E> {
        val res = result
        if (res != null) {
            if (res is ErrorResult) config.tryDispatch { callback(res.error) }
        } else {
            addFailCb(callback)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            val res2 = result
            if (res2 != null && res2 is ErrorResult) fire (failCbs, res2.error)
        }

        return this
    }

    override fun always(callback: () -> Unit): Promise<V, E> {

        if (result != null) {
            config.tryDispatch { callback() }
        } else {
            addAlwaysCb(callback)

            // we might have missed the result while adding to the list, therefor trigger
            // a (possible) second update.
            if (result != null) fire (alwaysCbs)
        }

        return this
    }

    private fun fire<T>(ref: ValueNode<(T) -> Unit>?, value: T) {
        ref.iterate {
            if (!it.done && it.trySetDone()) {
                val function = it.value
                config.tryDispatch { function(value) }
            }
        }
    }

    private fun fire(ref: ValueNode<() -> Unit>?) {
        ref.iterate {
            if (!it.done && it.trySetDone()) {
                val function = it.value
                config.tryDispatch { function() }
            }
        }
    }
}

private inline fun <T> ValueNode<T>?.iterate(cb: (ValueNode<T>) -> Unit) {
    var node = this
    while (node != null) {
        val n = node as ValueNode<T>
        cb(n)
        node = n.next
    }
}

private val <T : Any> ValueNode<T>.next: ValueNode<T>? get() = this.getNext()
private val <T : Any> ValueNode<T>.done: Boolean get() = this.isDone()
private val <T : Any> ValueNode<T>.value: T get() = this.getValue()


