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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package nl.komponents.kovenant.progress

import nl.komponents.kovenant.progress.JvmProgressPromise
import nl.komponents.kovenant.progress.ProgressPromise

trait ProgressPromise<V, E> : Promise<V, E> {
    val progress: Progress
}

public fun async<V>(context: Context = Kovenant.context, body: ProgressControl.() -> V): ProgressPromise<V, Exception> {
    val deferred = deferred<V, Exception>(context)
    val control = progressControl()
    context.workerDispatcher offer {
        try {
            val result = control.body()
            deferred.resolve(result)
            control.markAsDone()
        } catch(e: Exception) {
            deferred.reject(e)
        }
    }
    return JvmProgressPromise(deferred.promise, control.progress)
}

public fun <V, R> ProgressPromise<V, Exception>.then(bind: ProgressControl.(V) -> R): ProgressPromise<R, Exception> {
    val context = when (this) {
        is ContextAware -> this.context
        else -> Kovenant.context
    }

    val masterControl = progressControl()
    val children = progress.children
    if (children.isEmpty()) {
        masterControl addChild progress
    } else {
        children.forEach {
            masterControl.addChild(it.progress, it.weight)
        }
    }

    val contextControl = masterControl.createChild()
    val deferred = deferred<R, Exception>(context)
    success {
        context.workerDispatcher offer {
            try {
                val result = contextControl.bind(it)
                deferred.resolve(result)
                contextControl.markAsDone()
            } catch(e: Exception) {
                deferred.reject(e)
            }
        }
    }
    fail {
        deferred.reject(it)
    }
    return JvmProgressPromise(deferred.promise, masterControl.progress)
}


private class JvmProgressPromise<V, E>(private val promise: Promise<V, E>,
                                       override val progress: Progress) : ProgressPromise<V, E>, Promise<V, E> {
    override fun always(callback: () -> Unit): ProgressPromise<V, E> {
        promise always callback
        return this
    }

    override fun fail(callback: (E) -> Unit): ProgressPromise<V, E> {
        promise fail callback
        return this
    }

    override fun success(callback: (V) -> Unit): ProgressPromise<V, E> {
        promise success  callback
        return this
    }

}
