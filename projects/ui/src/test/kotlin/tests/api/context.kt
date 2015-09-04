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

package tests.ui.context

import nl.komponents.kovenant.Context
import nl.komponents.kovenant.Dispatcher
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.ui.KovenantUi
import nl.komponents.kovenant.ui.defaultDispatcherContextBuilder
import nl.komponents.kovenant.ui.successUi
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CacheBehaviourTest {
    val factory = defaultDispatcherContextBuilder()

    val dispatcher1 = StubDispatcher()
    val dispatcher2 = StubDispatcher()

    val context1 = createContext(dispatcher1)
    val context2 = createContext(dispatcher2)

    Test fun sameInstance() {
        val instance1 = factory(dispatcher1, context1)
        val instance2 = factory(dispatcher1, context1)

        assertEquals(instance1, instance2, "instances must match")
    }

    Test fun differentInstanceContext() {
        val instance1 = factory(dispatcher1, context1)
        val instance2 = factory(dispatcher1, context2)

        assertNotEquals(instance1, instance2, "instances must differ")
    }

    Test fun differentInstanceDispatcher() {
        val instance1 = factory(dispatcher1, context1)
        val instance2 = factory(dispatcher2, context1)

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

class MultipleContextsTest {
    val defaultDispatcher = ImmediateDispatcher()
    val alternateDispatcher = ImmediateDispatcher()
    var context = KovenantUi.uiContext //gets overridden by setup, avoids null checks

    Before fun setup() {
        KovenantUi.uiContext {
            dispatcher = defaultDispatcher
        }

        context = KovenantUi.createUiContext {
            dispatcher = alternateDispatcher
        }
    }

    Test fun defaultContext() {
        var defaultCalls = 0
        defaultDispatcher.onOffered = { ++defaultCalls }
        var alternateCalls = 0
        alternateDispatcher.onOffered = { ++alternateCalls }

        Promise.of(42).successUi { /*unimportant*/ }
        assertEquals(1, defaultCalls, "should be called on default dispatcher")
        assertEquals(0, alternateCalls, "should not be called on alternate dispatcher")
    }

    Test fun alternateContext() {
        var alternateCalls = 0
        alternateDispatcher.onOffered = { ++alternateCalls }
        var defaultCalls = 0
        defaultDispatcher.onOffered = { ++defaultCalls }

        Promise.of(42).successUi(uiContext = context, alwaysSchedule = false) { /*unimportant*/ }
        assertEquals(0, defaultCalls, "should not be called on default dispatcher")
        assertEquals(1, alternateCalls, "should be called on alternate dispatcher")
    }
}

private class ImmediateDispatcher(var onOffered: (task: () -> Unit) -> Unit = {}) : Dispatcher {
    override fun offer(task: () -> Unit): Boolean {
        onOffered(task)
        task()
        return true
    }
}

