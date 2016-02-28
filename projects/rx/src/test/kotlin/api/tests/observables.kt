/*
 * Copyright (c) 2016 Mark Platvoet<mplatvoet@gmail.com>
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

package api.tests.observables

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.rx.EmitStrategy
import nl.komponents.kovenant.rx.toListPromise
import nl.komponents.kovenant.rx.toPromise
import nl.komponents.kovenant.testMode
import org.junit.Before
import org.junit.Test
import rx.Observable
import kotlin.test.assertEquals
import kotlin.test.fail

class ToPromiseTest {

    @Before fun setup() {
        Kovenant.testMode {
            fail(it.message)
        }
    }

    @Test fun defaultMultipleValues() {
        var success = 0
        var failures = 0
        var result = -1
        Observable.from(arrayOf(1, 2, 3, 4, 5)).toPromise() success {
            result = it
            success++
        } fail {
            failures++
        }

        assertEquals(1, success, "success should be called once")
        assertEquals(0, failures, "failures should not be called")
        assertEquals(1, result, "should take first element from source")
    }

    @Test fun lastMultipleValues() {
        var success = 0
        var failures = 0
        var result = -1
        Observable.from(arrayOf(1, 2, 3, 4, 5)).toPromise(strategy = EmitStrategy.LAST) success {
            result = it
            success++
        } fail {
            failures++
        }

        assertEquals(1, success, "success should be called once")
        assertEquals(0, failures, "failures should not be called")
        assertEquals(5, result, "should take last element from source")
    }

    @Test fun defaultNoValues() {
        var success = 0
        var failures = 0
        Observable.from(arrayOf<Int>()).toPromise() success {

            success++
        } fail {
            failures++
        }

        assertEquals(0, success, "success should not be called")
        assertEquals(1, failures, "failures should be called once")
    }

    @Test fun fallbackValueNoValues() {
        var success = 0
        var failures = 0
        var result = -1
        Observable.from(arrayOf<Int>()).toPromise(42) success {
            result = it
            success++
        } fail {
            failures++
        }

        assertEquals(1, success, "success should be called once")
        assertEquals(0, failures, "failures should not be called")
        assertEquals(42, result, "default value should be used")
    }

    @Test fun fallbackFactoryNoValues() {
        var success = 0
        var failures = 0
        var result = -1
        Observable.from(arrayOf<Int>()).toPromise { 42 } success {
            result = it
            success++
        } fail {
            failures++
        }

        assertEquals(1, success, "success should be called once")
        assertEquals(0, failures, "failures should not be called")
        assertEquals(42, result, "default value should be used")
    }
}

class ToListPromiseTest {

    @Before fun setup() {
        Kovenant.testMode {
            fail(it.message)
        }
    }

    @Test fun multipleValues() {
        var success = 0
        var failures = 0
        var result = listOf(-1)
        Observable.from(arrayOf(1, 2, 3, 4, 5)).toListPromise() success {
            result = it
            success++
        } fail {
            failures++
        }

        assertEquals(1, success, "success should be called once")
        assertEquals(0, failures, "failures should not be called")
        assertEquals(listOf(1, 2, 3, 4, 5), result, "expect a list with results")
    }

    @Test fun noValues() {
        var success = 0
        var failures = 0
        var result = listOf(-1)
        Observable.from(arrayOf<Int>()).toListPromise() success {
            result = it
            success++
        } fail {
            failures++
        }

        assertEquals(1, success, "success should be called once")
        assertEquals(0, failures, "failures should not be called")
        assertEquals(listOf<Int>(), result, "expect an emoty list")
    }
}
