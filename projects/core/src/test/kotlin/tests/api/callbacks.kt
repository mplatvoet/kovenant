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

package tests.api.promises

import nl.komponents.kovenant.DirectDispatcher
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.task
import nl.komponents.kovenant.testMode
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PromiseCallbackTest {

    @Before fun setup() {
        Kovenant.testMode {
            kotlin.test.fail(it.message)
        }
    }

    @Test fun success() {
        var numberOfCalls = 0
        task { 13 } success { numberOfCalls++ }
        assertEquals(1, numberOfCalls, "success should be called once")
    }

    @Test fun alwaysSuccess() {
        var numberOfCalls = 0
        task { 13 } always { numberOfCalls++ }
        assertEquals(1, numberOfCalls, "success should be called once")
    }

    @Test fun fail() {
        var numberOfCalls = 0
        task { throw Exception() } fail { numberOfCalls++ }
        assertEquals(1, numberOfCalls, "fail should be called once")
    }

    @Test fun alwaysFail() {
        var numberOfCalls = 0
        task { throw Exception() } always { numberOfCalls++ }
        assertEquals(1, numberOfCalls, "always should be called once")
    }

    @Test fun multipleSuccesses() {
        var numberOfCalls = 0
        task { 13 } success { numberOfCalls++ } success { numberOfCalls++ } success { numberOfCalls++ }
        assertEquals(3, numberOfCalls, "success should be called 3 times")
    }

    @Test fun multipleFails() {
        var numberOfCalls = 0
        task { throw Exception() } fail { numberOfCalls++ } fail { numberOfCalls++ } fail { numberOfCalls++ }
        assertEquals(3, numberOfCalls, "fail should be called 3 times")
    }

    @Test fun mixedSuccessAndAlways() {
        var successess = 0
        var always = 0
        task { 13 } success { successess++ } always  { always++ } success { successess++ }
        assertEquals(2, successess, "success should be called 2 times")
        assertEquals(1, always, "always should be called once")
    }

    @Test fun mixedFailAndAlways() {
        var fails = 0
        var always = 0
        task { throw Exception() } fail { fails++ } always  { always++ } fail { fails++ }
        assertEquals(2, fails, "fail should be called 2 times")
        assertEquals(1, always, "always should be called once")
    }

    @Test fun eitherFail() {
        var fails = 0
        var success = 0
        task { throw Exception() } fail { fails++ } success { success++ }
        assertEquals(1, fails, "fail should be called 1 time")
        assertEquals(0, success, "success shouldn't be called")
    }

    @Test fun eitherSuccess() {
        var fails = 0
        var success = 0
        task { 13 } fail { fails++ } success { success++ }
        assertEquals(0, fails, "fail shouldn't be called")
        assertEquals(1, success, "success should be called 1 time")
    }
}

class PromiseCallbackOrderTest {

    @Before fun setup() {
        Kovenant.testMode {
            fail(it.message)
        }
    }

    @Test fun orderAlwaysSuccessAlways() {
        var calls = 0
        task { 13 } always {
            calls++
            assertEquals(1, calls, "Should be first")
        } success  {
            calls++
            assertEquals(2, calls, "Should be second")
        } always {
            calls++
            assertEquals(3, calls, "Should be third")
        }
        assertEquals(3, calls, "All should be called")
    }

    @Test fun orderSuccessSuccessAlways() {
        var calls = 0
        task { 13 } success {
            calls++
            assertEquals(1, calls, "Should be first")
        } success  {
            calls++
            assertEquals(2, calls, "Should be second")
        } always {
            calls++
            assertEquals(3, calls, "Should be third")
        }
        assertEquals(3, calls, "All should be called")
    }

    @Test fun orderAlwaysSuccessSuccess() {
        var calls = 0
        task { 13 } always {
            calls++
            assertEquals(1, calls, "Should be first")
        } success  {
            calls++
            assertEquals(2, calls, "Should be second")
        } success  {
            calls++
            assertEquals(3, calls, "Should be third")
        }
        assertEquals(3, calls, "All should be called")
    }

    @Test fun orderAlwaysFailAlways() {
        var calls = 0
        task { throw Exception() } always {
            calls++
            assertEquals(1, calls, "Should be first")
        } fail  {
            calls++
            assertEquals(2, calls, "Should be second")
        } always  {
            calls++
            assertEquals(3, calls, "Should be third")
        }
        assertEquals(3, calls, "All should be called")
    }

    @Test fun orderFailFailAlways() {
        var calls = 0
        task { throw Exception() } fail {
            calls++
            assertEquals(1, calls, "Should be first")
        } fail  {
            calls++
            assertEquals(2, calls, "Should be second")
        } always  {
            calls++
            assertEquals(3, calls, "Should be third")
        }
        assertEquals(3, calls, "All should be called")
    }

    @Test fun orderAlwaysFailFail() {
        var calls = 0
        task { throw Exception() } always {
            calls++
            assertEquals(1, calls, "Should be first")
        } fail  {
            calls++
            assertEquals(2, calls, "Should be second")
        } fail  {
            calls++
            assertEquals(3, calls, "Should be third")
        }
        assertEquals(3, calls, "All should be called")
    }

}

