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

package tests.api.functional.map

import nl.komponents.kovenant.DirectDispatcher
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.functional.map
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MapTest {
    Before fun setup() {
        Kovenant.context {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }
    }

    Test fun mapSuccess() {
        var result = 0
        Promise.of(13) map { it + 2 } success { result = it }
        assertEquals(15, result, "should chain")
    }

    Test fun mapFail() {
        var count = 0
        Promise.of(13) map { throw Exception() } fail { count++ }
        assertEquals(1, count, "should report a failure")
    }
}

class MapContextTest {
    val defaultContext = Kovenant.context {
        callbackContext.dispatcher = DirectDispatcher.instance
        workerContext.dispatcher = DirectDispatcher.instance
    }

    val alternativeContext = Kovenant.createContext {
        callbackContext.dispatcher = DirectDispatcher.instance
        workerContext.dispatcher = DirectDispatcher.instance
    }

    Test fun defaultContext() {
        val p = Promise.of(13) map { it + 2 }
        assertEquals(defaultContext, p.context, "Expected the default context")
    }

    Test fun alternativeFirstContext() {
        val p = Promise.of(13, alternativeContext) map { it + 2 }
        assertEquals(alternativeContext, p.context, "Expected the alternative context")
    }

    Test fun specifiedContext() {
        val p = Promise.of(13).map(alternativeContext) { it + 2 }
        assertEquals(alternativeContext, p.context, "Expected the alternative context")
    }
}