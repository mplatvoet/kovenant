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

package tests.api.then

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.async
import nl.komponents.kovenant.then
import org.junit.Before
import org.junit.Test
import tests.support.ImmediateDispatcher
import kotlin.test.assertEquals

class ThenTest {

    Before fun setup() {
        Kovenant.configure {
            callbackDispatcher = ImmediateDispatcher()
            workerDispatcher = ImmediateDispatcher()
        }
    }

    Test fun thenSuccess() {
        var result = 0
        async { 13 } then {it + 2} success { result = it }
        assertEquals(15, result, "should chain")
    }

    Test fun thenFail() {
        var count = 0
        async { 13 } then {throw Exception()} fail { count++ }
        assertEquals(1, count, "should report a failure")
    }


}



