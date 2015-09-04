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

package tests.api.contexts

import nl.komponents.kovenant.Dispatcher
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.async
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class MultipleContextsTest {
    val defaultDispatcher = ImmediateDispatcher()
    val alternateDispatcher = ImmediateDispatcher()
    var context = Kovenant.context //gets overridden by setup, avoids null checks

    Before fun setup() {
        Kovenant.context {
            callbackContext {
                dispatcher = defaultDispatcher
            }
            workerContext {
                dispatcher = defaultDispatcher
            }
        }

        context = Kovenant.createContext {
            callbackContext.dispatcher = alternateDispatcher
            workerContext.dispatcher = alternateDispatcher
        }
    }

    Test fun defaultContext() {
        var calls = 0
        defaultDispatcher.onOffered = { ++calls }
        async { 13 }
        assertEquals(1, calls, "should by called on default dispatcher")
    }

    Test fun alternateContext() {
        var calls = 0
        alternateDispatcher.onOffered = { ++calls }
        async(context) { 13 }
        assertEquals(1, calls, "should by called on default dispatcher")
    }
}

private class ImmediateDispatcher(var onOffered: (task: () -> Unit) -> Unit = {}) : Dispatcher {
    override fun offer(task: () -> Unit): Boolean {
        onOffered(task)
        task()
        return true
    }
}

