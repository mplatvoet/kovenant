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

package tests.api.functional.bind

import nl.komponents.kovenant.DirectDispatcher
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.functional.bind
import nl.komponents.kovenant.testMode
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BindTest {
    @Before fun setup() {
        Kovenant.testMode {
            fail(it.message)
        }
    }

    @Test fun bindSuccess() {
        fun timesTwo(i: Int): Promise<Int, Exception> = Promise.of(i * 2)

        var result = 0
        Promise.of(13) bind { timesTwo(it) } success { result = it }
        assertEquals(26, result, "should chain")
    }

    @Test fun bindFail() {
        fun error(): Promise<Int, Exception> = Promise.ofFail(Exception())
        var count = 0
        Promise.of(13) bind  { error() } fail { count++ }
        assertEquals(1, count, "should report a failure")
    }
}

class BindContextTest {
    val defaultContext = Kovenant.context {
        callbackContext.dispatcher = DirectDispatcher.instance
        workerContext.dispatcher = DirectDispatcher.instance
    }

    val alternativeContext = Kovenant.createContext {
        callbackContext.dispatcher = DirectDispatcher.instance
        workerContext.dispatcher = DirectDispatcher.instance
    }

    @Test fun defaultContext() {
        fun timesTwo(i: Int): Promise<Int, Exception> = Promise.of(i * 2)
        val p = Promise.of(13) bind { timesTwo(it) }
        assertEquals(defaultContext, p.context, "Expected the default context")
    }

    @Test fun alternativeSecondContext() {
        fun timesTwo(i: Int): Promise<Int, Exception> = Promise.of(i * 2, alternativeContext)
        val p = Promise.of(13) bind { timesTwo(it) }
        assertEquals(defaultContext, p.context, "Expected the default context")
    }

    @Test fun alternativeFirstContext() {
        fun timesTwo(i: Int): Promise<Int, Exception> = Promise.of(i * 2)
        val p = Promise.of(13, alternativeContext) bind { timesTwo(it) }
        assertEquals(alternativeContext, p.context, "Expected the alternative context")
    }

    @Test fun specifiedContext() {
        fun timesTwo(i: Int): Promise<Int, Exception> = Promise.of(i * 2)
        val p = Promise.of(13).bind(alternativeContext) { timesTwo(it) }
        assertEquals(alternativeContext, p.context, "Expected the alternative context")
    }
}