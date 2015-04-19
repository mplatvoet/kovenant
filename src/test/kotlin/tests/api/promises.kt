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

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.async
import org.junit.Before
import org.junit.Test
import tests.support.ImmediateDispatcher
import kotlin.test.assertEquals

class PromiseTest {

    Before fun setup() {
        Kovenant.configure {
            callbackDispatcher = ImmediateDispatcher()
            workerDispatcher = ImmediateDispatcher()
        }
    }

    Test fun testSuccess() {
        var numberOfCalls = 0
        async { 13 } success { numberOfCalls++ }
        assertEquals(1, numberOfCalls, "success should be called once")
    }

    Test fun testAlwaysSuccess() {
        var numberOfCalls = 0
        async { 13 } always { numberOfCalls++ }
        assertEquals(1, numberOfCalls, "success should be called once")
    }

    Test fun testFail() {
        var numberOfCalls = 0
        async { throw Exception() } fail { numberOfCalls++ }
        assertEquals(1, numberOfCalls, "fail should be called once")
    }

    Test fun testAlwaysFail() {
        var numberOfCalls = 0
        async { throw Exception() } always { numberOfCalls++ }
        assertEquals(1, numberOfCalls, "always should be called once")
    }

}

