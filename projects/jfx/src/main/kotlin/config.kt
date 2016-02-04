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
@file:JvmName("KovenantJfx")
package nl.komponents.kovenant.jfx

import nl.komponents.kovenant.Dispatcher
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.ui.KovenantUi
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference


private val initCount = AtomicInteger(0)
private val disposable = AtomicReference<Disposable>(null)

fun startKovenant() {
    initCount.onlyFirst {
        disposable.set(configureKovenant())
    }
}

@JvmOverloads fun stopKovenant(force: Boolean = false) {
    val dispose = disposable.get()
    if (dispose != null && disposable.compareAndSet(dispose, null)) {
        dispose.close(force)
        initCount.set(0)
    }
}

/**
 * Configures Kovenant for common JavaFX scenarios.
 *
 * @return `Disposable` to properly shutdown Kovenant
 */
fun configureKovenant(): Disposable {
    KovenantUi.uiContext {
        dispatcher = JFXDispatcher.instance
    }

    val ctx = Kovenant.context {
        callbackContext.dispatcher {
            name = "kovenant-callback"
            concurrentTasks = 1

            pollStrategy {
                yielding(numberOfPolls = 100)
                blocking()
            }
        }
        workerContext.dispatcher {
            name = "kovenant-worker"

            pollStrategy {
                yielding(numberOfPolls = 100)
                blocking()
            }
        }
    }
    return DispatchersDisposable(ctx.workerContext.dispatcher, ctx.callbackContext.dispatcher)
}


/**
 * Disposes of a resource.
 *
 */
interface Disposable {
    fun close(force: Boolean = false)
}

private class DispatchersDisposable(private vararg val dispatcher: Dispatcher) : Disposable {
    override fun close(force: Boolean) {
        dispatcher.forEach {
            close(force, it)
        }
    }

    private fun close(force: Boolean, dispatcher: Dispatcher) {
        try {
            if (force) {
                dispatcher.stop(force = true)
            } else {
                dispatcher.stop(block = true)
            }
        } catch(e: Exception) {
            //ignore, nothing we can do
        }
    }

}

private inline fun AtomicInteger.onlyFirst(body: () -> Unit) {
    val threadNumber = incrementAndGet()
    if (threadNumber == 1) {
        body()
    } else {
        decrementAndGet()
    }
}



