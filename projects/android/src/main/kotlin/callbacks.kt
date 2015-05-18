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

package nl.mplatvoet.komponents.kovenant.android

import nl.mplatvoet.komponents.kovenant.*
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference


public fun <V> promiseOnUi(context: Context = Kovenant.context, body: () -> V): Promise<V, Exception> {
    val deferred = deferred<V, Exception>(context)
    LooperExecutor.main submit PromiseUiRunnable(deferred, body)
    return deferred.promise
}


public fun <V, E> Promise<V, E>.successUi(body: (value: V) -> Unit): Promise<V, E> {
    val dispatcherContext = CallbackContextCache forContext context
    return success(dispatcherContext, body)
}


public fun <V, E> Promise<V, E>.failUi(body: (error: E) -> Unit): Promise<V, E> {
    val dispatcherContext = CallbackContextCache forContext context
    return fail(dispatcherContext, body)
}


public fun <V, E> Promise<V, E>.alwaysUi(body: () -> Unit): Promise<V, E> {
    val dispatcherContext = CallbackContextCache forContext context
    return always(dispatcherContext, body)
}


private class DelegatingDispatcherContext(private val base: DispatcherContext, override val dispatcher: Dispatcher) : DispatcherContext {
    override val errorHandler: (Exception) -> Unit
        get() = base.errorHandler
}

private class PromiseUiRunnable<V>(private val deferred: Deferred<V, Exception>,
                                   private val body: () -> V) : Runnable {
    override fun run() = try {
        val result = body()
        deferred.resolve(result)
    } catch(e: Exception) {
        deferred.reject(e)
    }
}

/* A lot of assumptions are made for this class.
 * Assuming that during the lifetime of the Android app the creation of different contexts is sparse. Therefor
 * the number of active contexts will be limited. At most a couple of context die during the lifetime of the
 * Android app, thus the leftovers aren't in the way. Assuming that iteration is faster than using a concurrent
 * hashmap for this particular case since we mostly hit our target on first or second iteration.
 *
 * Not using ConcurrentLinkedQueue since iteration creates a new Iterator instance every single time. Then we would
 * rather just create a new DelegatingDispatcherContext every single call.
 *
 * Also, this cache does not guard for duplicates.
 *
 * If we come across a cleared node we discard the cache
 */
private object CallbackContextCache {
    private val head = AtomicReference<CacheNode>(null)

    fun forContext(context: Context): DispatcherContext {
        iterate {
            if (it.context identityEquals context) return it.dispatcherContext
        }
        val dispatcherContext = DelegatingDispatcherContext(context.callbackContext, androidUiDispatcher())
        add(context, dispatcherContext)
        return dispatcherContext
    }

    private class CacheNode(cacheItem: CacheItem) {
        //Use a weak reference, as long as the context lives (e.g. Kovenant.context) we have strong reference.
        //Android most recent gc uses some eager cleaning so discarded context get cleaned up pretty quickly
        val next = AtomicReference<CacheNode>(null)
        private val reference = WeakReference(cacheItem)
        val cacheItem: CacheItem? get() = reference.get()
    }

    // Let the Storm Troopers that call themselves Software Craftsmen go berserk
    // over this next piece. For Jedi eyes only.
    //
    // This does not only iterate but also clears the cache is we hit
    // a cleared reference.
    private inline fun iterate(fn: (CacheItem) -> Unit) {
        val headNode = head.get()
        if (headNode != null) {
            var node = headNode
            while (true) {
                val cacheItem = node.cacheItem
                if (cacheItem == null) {
                    //one of the cache items is null
                    //discard the whole cache and rebuild
                    head.set(null)
                    break
                }

                fn(cacheItem)

                val next = node.next.get()
                if (next == null) break
                node = next
            }
        }
    }

    fun add(context: Context, dispatcherContext: DispatcherContext) {
        val item = CacheItem(context, dispatcherContext)
        add(CacheNode(item))
    }

    private fun add(node: CacheNode) {
        while (true) {
            val headNode = head.get()
            if (headNode == null) {
                if (head.compareAndSet(null, node)) break
            } else {
                do {
                    val tail = tailNode(headNode)
                } while (!tail.next.compareAndSet(null, node))
                break
            }
        }
    }

    private fun tailNode(head: CacheNode): CacheNode {
        var tail = head
        while (true) {
            val next = tail.next.get()
            if (next == null) {
                return tail
            }
            tail = next
        }
    }

    private class CacheItem(val context: Context, val dispatcherContext: DispatcherContext)
}
