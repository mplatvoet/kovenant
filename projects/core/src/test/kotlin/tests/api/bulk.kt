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

package tests.api.bulk

import nl.komponents.kovenant.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AllTest {

    @Before fun setup() {
        Kovenant.context {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }
    }

    @Test fun emptyCallVerify() {
        var success = 0
        var fails = 0
        all<Int>() success { success++ } fail { fails++ }
        assertEquals(1, success, "success should be called once")
        assertEquals(0, fails, "fail should not be called")
    }

    @Test fun oneCallVerify() {
        var success = 0
        var fails = 0
        all(Promise.of(13)) success { success++ } fail { fails++ }
        assertEquals(1, success, "success should be called once")
        assertEquals(0, fails, "fail should not be called")
    }

    @Test fun oneCallErrorVerify() {
        var success = 0
        var fails = 0
        all<Int>(Promise.ofFail(Exception())) success { success++ } fail { fails++ }
        assertEquals(0, success, "success should not be called")
        assertEquals(1, fails, "fail should be called once")
    }

    @Test fun nullCallVerify() {
        var success = 0
        var fails = 0
        all(Promise.of(null)) success { success++ } fail { fails++ }
        assertEquals(1, success, "success should be called once")
        assertEquals(0, fails, "fail should not be called")
    }

    @Test fun multipleCallVerify() {
        var success = 0
        var fails = 0
        all(Promise.of(13), Promise.of(13), Promise.of(13)) success { success++ } fail { fails++ }
        assertEquals(1, success, "success should be called once")
        assertEquals(0, fails, "fail should not be called")
    }

    @Test fun multipleCallErrorVerify() {
        var success = 0
        var fails = 0
        all<Int>(Promise.ofFail(Exception()), Promise.ofFail(Exception()), Promise.ofFail(Exception())) success { success++ } fail { fails++ }
        assertEquals(0, success, "success should not be called")
        assertEquals(1, fails, "fail should be called once")
    }

    @Test fun emptyCallResult() {
        all<Int>() success {
            assertEquals(0, it.size(), "result list should be empty")
        }
    }

    @Test fun oneCallResult() {
        all(Promise.of(13)) success {
            assertEquals(1, it.size(), "result list should be size == 1")
        }
    }

    @Test fun multipleCallResult() {
        all(Promise.of(13), Promise.of(13), Promise.of(13)) success {
            assertEquals(3, it.size(), "result list should be size == 3")
        }
    }

    @Test fun multipleCallResultOrder() {
        all(Promise.of(5), Promise.of(9), Promise.of(8)) success {
            assertEquals(5, it[0], "wrong order")
            assertEquals(9, it[1], "wrong order")
            assertEquals(8, it[2], "wrong order")
        }
    }


    @Test fun properContextMultiple() {
        val context = Kovenant.createContext {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }

        val p = all(Promise.of(13), Promise.of(9), Promise.of(8), context = context)
        assertEquals(context, p.context, "Wrong context")
    }

    @Test fun properContextEmpty() {
        val context = Kovenant.createContext {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }

        val p = all<Int>(context = context)
        assertEquals(context, p.context, "Wrong context")
    }
}

class AnyTest {

    @Before fun setup() {
        Kovenant.context {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }
    }

    @Test fun emptyCallVerify() {
        var success = 0
        var fails = 0
        any<Int>() success { success++ } fail { fails++ }
        assertEquals(1, fails, "fails should be called once")
        assertEquals(0, success, "success should not be called")
    }

    @Test fun oneCallVerify() {
        var success = 0
        var fails = 0
        any(Promise.of(13)) success { success++ } fail { fails++ }
        assertEquals(1, success, "success should be called once")
        assertEquals(0, fails, "fails should not be called")
    }

    @Test fun nullCallVerify() {
        var success = 0
        var fails = 0
        any(Promise.of(null)) success { success++ } fail { fails++ }
        assertEquals(1, success, "success should be called once")
        assertEquals(0, fails, "fails should not be called")
    }

    @Test fun oneCallErrorVerify() {
        var success = 0
        var fails = 0
        any<Int>(Promise.ofFail(Exception())) success { success++ } fail { fails++ }
        assertEquals(1, fails, "fails should be called once")
        assertEquals(0, success, "success should not be called")
    }

    @Test fun multipleCallVerify() {
        var success = 0
        var fails = 0
        any(Promise.of(13), Promise.of(13), Promise.of(13)) success { success++ } fail { fails++ }
        assertEquals(1, success, "success should be called once")
        assertEquals(0, fails, "fails should not be called")
    }

    @Test fun emptyCallResult() {
        any<Int>() fail {
            assertEquals(0, it.size(), "result list should be empty")
        }
    }

    @Test fun oneCallResult() {
        any<Int>(Promise.ofFail(Exception())) fail {
            assertEquals(1, it.size(), "result list should be size == 1")
        }
    }

    @Test fun multipleErrorResult() {
        any<Int>(Promise.ofFail(Exception()), Promise.ofFail(Exception()), Promise.ofFail(Exception())) fail  {
            assertEquals(3, it.size(), "result list should be size == 3")
        }
    }

    @Test fun multipleCallErrorOrder() {
        val one = Exception()
        val two = Exception()
        val three = Exception()
        any<Int>(Promise.ofFail(one), Promise.ofFail(two), Promise.ofFail(three)) fail {
            assertEquals(one, it[0], "wrong order")
            assertEquals(two, it[1], "wrong order")
            assertEquals(three, it[2], "wrong order")
        }
    }

    @Test fun properContextMultiple() {
        val context = Kovenant.createContext {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }

        val p = any(Promise.of(13), Promise.of(9), Promise.of(8), context = context)
        assertEquals(context, p.context, "Wrong context")
    }

    @Test fun properContextEmpty() {
        val context = Kovenant.createContext {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }

        val p = any<Int>(context = context)
        assertEquals(context, p.context, "Wrong context")
    }
}
