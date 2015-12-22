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
package tests.api.casts

import nl.komponents.kovenant.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CastTest {

    @Before fun setup() {
        Kovenant.context {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }
    }

    @Test fun taskPromise() {
        val promise = task { 13 }
        assertFalse(promise is Deferred<*,*>, "Promise created by task shouldn't be castable to a Deferred")
    }

    @Test fun thenPromise() {
        val promise = task { 13 }.then { 14 }
        assertFalse(promise is Deferred<*,*>, "Promise created by then shouldn't be castable to a Deferred")
    }

    @Test fun deferredPromise() {
        val promise = deferred<Int, Int>().promise
        assertFalse(promise is Deferred<*,*>, "Promise created by 'deferred()' shouldn't be castable to a Deferred")
    }

    @Test fun promiseDeferred() {
        val deferred = deferred<Int, Int>()
        assertTrue(deferred is Promise<*,*>, "Deferred created by 'deferred()' should be castable to a Promise")
    }
}