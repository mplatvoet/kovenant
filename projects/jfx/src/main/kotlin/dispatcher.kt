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
package nl.komponents.kovenant.jfx

import javafx.application.Platform
import nl.komponents.kovenant.Dispatcher


public class JFXDispatcher private constructor() : Dispatcher {
    companion object {
        val instance: JFXDispatcher = JFXDispatcher()
    }

    override val stopped: Boolean get() = throw UnsupportedOperationException()
    override val terminated: Boolean get() = throw UnsupportedOperationException()

    override fun offer(task: () -> Unit): Boolean {
        Platform.runLater(task)
        return true
    }

    override fun stop(force: Boolean, timeOutMs: Long, block: Boolean): List<() -> Unit> = throw UnsupportedOperationException()
    override fun tryCancel(task: () -> Unit): Boolean = throw UnsupportedOperationException()

    fun currentIsUiThread(): Boolean = Platform.isFxApplicationThread()
}