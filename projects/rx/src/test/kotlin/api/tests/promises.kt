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

package tests.api.promises

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.rx.toObservable
import nl.komponents.kovenant.testMode
import org.junit.Before
import org.junit.Test
import rx.Observer
import kotlin.test.assertEquals
import kotlin.test.fail

class ToObservableTest {

    @Before fun setup() {
        Kovenant.testMode {
            fail(it.message)
        }
    }

    @Test fun successResult() {
        var success = 0
        Promise.of(42).toObservable().subscribe {
            success++
        }
        assertEquals(1, success, "success should be called once")
    }

    @Test fun failResult() {
        var success = 0
        var failures = 0
        Promise.ofFail<Int, Exception>(Exception()).toObservable().subscribe(object : Observer<Int> {
            override fun onCompleted() {
            }

            override fun onNext(value: Int) {
                success++
            }

            override fun onError(error: Throwable) {
                failures++
            }
        })
        assertEquals(0, success, "success should not be called")
        assertEquals(1, failures, "failures should be called once")
    }


    @Test fun withUnresolved() {
        var success = 0
        var failures = 0
        deferred<Int, Exception>().promise.toObservable().subscribe(object : Observer<Int> {
            override fun onCompleted() {
            }

            override fun onNext(value: Int) {
                success++
            }

            override fun onError(error: Throwable) {
                failures++
            }
        })
        assertEquals(0, success, "success should not be called")
        assertEquals(0, failures, "failures should not be called")
    }


    @Test fun delayedSuccessResolve() {
        var success = 0
        var failures = 0
        val deferred = deferred<Int, Exception>()
        deferred.promise.toObservable().subscribe(object : Observer<Int> {
            override fun onCompleted() {
            }

            override fun onNext(value: Int) {
                success++
            }

            override fun onError(error: Throwable) {
                failures++
            }
        })
        deferred.resolve(42)
        assertEquals(1, success, "success should be called once")
        assertEquals(0, failures, "failures should not be called")
    }

    @Test fun delayedFailsResolve() {
        var success = 0
        var failures = 0
        val deferred = deferred<Int, Exception>()
        deferred.promise.toObservable().subscribe(object : Observer<Int> {
            override fun onCompleted() {
            }

            override fun onNext(value: Int) {
                success++
            }

            override fun onError(error: Throwable) {
                failures++
            }
        })
        deferred.reject(Exception())
        assertEquals(0, success, "success should not be called")
        assertEquals(1, failures, "failures should be called once")
    }
}
