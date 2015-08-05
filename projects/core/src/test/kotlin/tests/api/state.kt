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

package tests.api.state

import nl.komponents.kovenant.DirectDispatcher
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.deferred
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StateTest {

    Before fun setup() {
        Kovenant.context {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }
    }

    Test fun uncompleted() {
        val deferred = deferred<Int, Exception>()
        assertFalse(deferred.promise.isDone(), "Should not be done")
        assertFalse(deferred.promise.isSuccess(), "Should not be successful")
        assertFalse(deferred.promise.isFailure(), "Should not be a failure")
    }

    Test fun success() {
        val deferred = deferred<Int, Exception>()
        deferred.resolve(13)
        assertTrue(deferred.promise.isDone(), "Should be done")
        assertTrue(deferred.promise.isSuccess(), "Should be successful")
        assertFalse(deferred.promise.isFailure(), "Should not be a failure")
    }

    Test fun failure() {
        val deferred = deferred<Int, Exception>()
        deferred.reject(Exception())
        assertTrue(deferred.promise.isDone(), "Should be done")
        assertFalse(deferred.promise.isSuccess(), "Should not be successful")
        assertTrue(deferred.promise.isFailure(), "Should be a failure")
    }
}

