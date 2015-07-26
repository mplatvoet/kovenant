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

package tests.api.functional

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.functional.unwrap
import org.junit.Before
import org.junit.Test
import tests.support.ImmediateDispatcher
import kotlin.test.assertEquals

class UnwrapTest {

    Before fun setup() {
        Kovenant.context {
            callbackContext.dispatcher = ImmediateDispatcher()
            workerContext.dispatcher = ImmediateDispatcher()
        }
    }

    Test fun successUnwrap() {
        var success = 0
        var fails = 0
        Promise.of(Promise.of(42)).unwrap() success {
            assertEquals(42, it, "should be unwrapped")
            success++
        } fail { fails++ }
        assertEquals(1, success, "success should be called once")
        assertEquals(0, fails, "fail should not be called")
    }
}

class UnwrapContextTest {
    val defaultContext = Kovenant.context {
        callbackContext.dispatcher = ImmediateDispatcher()
        workerContext.dispatcher = ImmediateDispatcher()
    }

    val alternativeContext = Kovenant.createContext {
        callbackContext.dispatcher = ImmediateDispatcher()
        workerContext.dispatcher = ImmediateDispatcher()
    }

    Test fun defaultContext() {
        val nestedPromise = Promise.of(42)
        val unwrapped = Promise.of(nestedPromise).unwrap()
        assertEquals(defaultContext, unwrapped.context, "Expected the default context")
    }

    Test fun alternativeNestedContext() {
        val nestedPromise = Promise.of(42, alternativeContext)
        val unwrapped = Promise.of(nestedPromise).unwrap()
        assertEquals(defaultContext, unwrapped.context, "Expected the default context")
    }

    Test fun alternativeUnwrappedContext() {
        val nestedPromise = Promise.of(42)
        val unwrapped = Promise.of(nestedPromise, alternativeContext).unwrap()
        assertEquals(alternativeContext, unwrapped.context, "Expected the alternative context")
    }

    Test fun specifiedContext() {
        val nestedPromise = Promise.of(42)
        val unwrapped = Promise.of(nestedPromise).unwrap(alternativeContext)
        assertEquals(alternativeContext, unwrapped.context, "Expected the alternative context")
    }
}

