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

import nl.komponents.kovenant.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.fail

class GetTest {

    @Before fun setup() {
        Kovenant.context {
            callbackContext.dispatcher = DirectDispatcher.instance
            workerContext.dispatcher = DirectDispatcher.instance
        }
    }

    @Test fun successResult() {
        val promise = Promise.of(13)
        assertEquals(13, promise.get(), "should return the proper value")
    }

    @Test fun errorResult() {
        val promise = Promise.ofFail<Int, Int>(13)
        assertEquals(13, promise.getError(), "should return the proper value")
    }

    @Test fun nullSuccessResult() {
        val promise = Promise.of(null)
        assertEquals(null, promise.get(), "should return null")
    }

    @Test fun nullErrorResult() {
        val promise = Promise.ofFail<Int, Int?>(null)
        assertEquals(null, promise.getError(), "should return null")
    }

    @Test fun failResultException() {
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
        assert(thrown) { "should throw an exception" }
    }

    @Test fun failResultValue() {
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
        assert(thrown) { "should throw a FailedException" }
    }
}

class GetAsyncTest {


    @Before fun setup() {
        Kovenant.context {
            val dispatcher = buildDispatcher { concurrentTasks = 1 }
            callbackContext.dispatcher = dispatcher
            workerContext.dispatcher = dispatcher
        }
    }

    @After fun shutdown() {
        Kovenant.stop()
    }

    @Test(timeout = 10000) fun blockingGet() {
        val deferred = deferred<Int, Exception>()

        verifyBlocking({ deferred.resolve(42) }) {
            try {
                val i = deferred.promise.get()
                assertEquals(42, i, "Should succeed")
            } catch(e: Exception) {
                fail("Should not fail")
            }
        }
    }


    private fun verifyBlocking(trigger: () -> Unit, blockingAction: () -> Unit) {
        val startLatch = CountDownLatch(1)
        val stopLatch = CountDownLatch(1)
        val ref = AtomicReference<Throwable>()
        val thread = Thread {
            startLatch.countDown()
            try {
                blockingAction()
            } catch (err: Throwable) {
                ref.set(err)
            } finally {
                stopLatch.countDown()
            }
        }
        thread.start()

        startLatch.await()
        loop@while (true) when (thread.getState()) {
            Thread.State.BLOCKED, Thread.State.WAITING, Thread.State.TIMED_WAITING -> break@loop
            Thread.State.TERMINATED -> break@loop
            else -> Thread.yield()
        }
        trigger()
        stopLatch.await()
        val throwable = ref.get()
        if (throwable != null) {
            throw throwable
        }
    }


}
