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
package nl.komponents.kovenant.ui

import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference


//TODO hook up the cache

/**
 * A special kind of weak reference cache. The key is weakly referenced and if,
 * during iteration, we come across a cleared key the *whole* cache gets invalidated.
 *
 * Key lookup is always done by iteration. So best performance is achieved when number of elements
 * are kept to a minimum and it is expected that keys are reasonably long lived.
 *
 * Iteration is done without introducing garbage opposed to the standard JVM concurrent structures.
 */
private class WeakReferenceCache<K : Any, V : Any>(private val factory: (K) -> V) {
    private val head = AtomicReference<CacheNode<K, V>>(null)

    fun get(key: K): V {
        iterate {
            k, v ->
            if (k == key) return v
        }
        val value = factory(key)
        add(key, value)
        return value
    }

    private class CacheNode<K : Any, V : Any>(key: K, val value: V) {
        val next = AtomicReference<CacheNode<K, V>>(null)
        private val keyRef = WeakReference(key)

        val key: K? get() = keyRef.get()
    }

    // Let the Storm Troopers that call themselves Software Craftsmen go berserk
    // over this next piece. So, for Jedi eyes only. ;-)
    //
    // This does not only iterate but also clears the cache if we hit
    // a cleared reference.
    private inline fun iterate(fn: (K, V) -> Unit) {
        val headNode = head.get()
        if (headNode != null) {
            var node = headNode
            while (true) {
                val key = node.key
                if (key == null) {
                    // one of the cache items is null
                    // discard the whole cache and rebuild
                    head.set(null)
                    break
                }

                fn(key, node.value)

                val next = node.next.get()
                if (next == null) break
                node = next
            }
        }
    }

    fun add(key: K, value: V) {
        add(CacheNode(key, value))
    }

    private fun add(node: CacheNode<K, V>) {
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

    private fun tailNode(head: CacheNode<K, V>): CacheNode<K, V> {
        var tail = head
        while (true) {
            val next = tail.next.get()
            if (next == null) {
                return tail
            }
            tail = next
        }
    }
}