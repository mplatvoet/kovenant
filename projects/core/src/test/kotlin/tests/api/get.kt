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

package tests.api.get

import nl.komponents.kovenant.FailedException
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import org.junit.Before
import org.junit.Test
import tests.support.ImmediateDispatcher
import kotlin.test.assertEquals
import kotlin.test.fail

class GetTest {

    Before fun setup() {
        Kovenant.context {
            callbackContext.dispatcher = ImmediateDispatcher()
            workerContext.dispatcher = ImmediateDispatcher()
        }
    }

    Test fun successResult() {
        val promise = Promise.of(13)
        assertEquals(13, promise.get(), "should return the proper value")
    }

    Test fun failResultException() {
        val promise = Promise.ofFail<Int, Exception>(Exception("bummer"))
        var thrown = false
        try {
            promise.get()
            fail("Should not be reachable")
        } catch(e: FailedException) {
            fail("Exception should not be wrapped")
        } catch(e: Exception) {
            thrown = true
        }
        assert(thrown, "should throw an exception")
    }

    Test fun failResultValue() {
        val promise = Promise.ofFail<Int, String>("bummer")
        var thrown = false
        try {
            promise.get()
            fail("Should not be reachable")
        } catch(e: FailedException) {
            thrown = true
        } catch(e: Exception) {
            fail("Should be of type FailedException")
        }
        assert(thrown, "should throw a FailedException")
    }
}

