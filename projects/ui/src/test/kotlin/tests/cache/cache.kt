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

package tests.ui.cache

import nl.komponents.kovenant.Context
import nl.komponents.kovenant.Dispatcher
import nl.komponents.kovenant.DispatcherContext
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.ui.DelegatingDispatcherContext
import nl.komponents.kovenant.ui.WeakReferenceCache
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CacheConfigurationTest {
    val cache = WeakReferenceCache<Dispatcher, WeakReferenceCache<Context, DispatcherContext>>() {
        dispatcher ->
        WeakReferenceCache<Context, DispatcherContext>() {
            context ->
            DelegatingDispatcherContext(context.callbackContext, dispatcher)
        }
    };

    val dispatcher1 = StubDispatcher()
    val dispatcher2 = StubDispatcher()

    val context1 = createContext(dispatcher1)
    val context2 = createContext(dispatcher2)

    Test fun sameInstance() {
        val instance1 = cache[dispatcher1][context1]
        val instance2 = cache[dispatcher1][context1]

        assertEquals(instance1, instance2, "instances must match")
    }

    Test fun differentInstanceContext() {
        val instance1 = cache[dispatcher1][context1]
        val instance2 = cache[dispatcher1][context2]

        assertNotEquals(instance1, instance2, "instances must differ")
    }

    Test fun differentInstanceDispatcher() {
        val instance1 = cache[dispatcher1][context1]
        val instance2 = cache[dispatcher2][context2]

        assertNotEquals(instance1, instance2, "instances must differ")
    }

    fun createContext(dispatcher: Dispatcher): Context = Kovenant.createContext {
        callbackContext.dispatcher = dispatcher
        workerContext.dispatcher = dispatcher
    }

    class StubDispatcher : Dispatcher {
        override fun offer(task: () -> Unit): Boolean = throw UnsupportedOperationException()
    }
}

