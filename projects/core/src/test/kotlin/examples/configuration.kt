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

package examples.configuration

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.buildDispatcher


fun main (args: Array<String>) {
    Kovenant.configure {
        // Specify a new worker dispatcher.
        // this dispatcher is responsible for
        // work that is executed by async and
        // then functions so this is basically
        // work that is expected to run a bit
        // longer
        workerContext.dispatcher = buildDispatcher {
            // Name this dispatcher, threads
            // created by this dispatcher will
            // get this name with a number
            // appended
            name = "Bob the builder"

            // the max number of threads this
            // dispatcher keeps running in parallel.
            // During the lifetime of this
            // dispatcher the number of threads
            // created can be far greater because
            // threads also get destroyed.
            numberOfThreads = 2

            // Configure the strategy to apply
            // to a thread when there is no work
            // left in the queue. Note that
            // when the strategy finishes the
            // thread will shutdown. Strategies are
            // applied in order of configuration and
            // resets after a thread executes any
            // new task.
            configurePollStrategy {
                // A busy poll strategy simple polls
                // the provided amount of polls
                // without interrupting the thread.
                yielding(numberOfPolls = 1000)

                // A sleep poll strategy simply puts
                // the thread to sleep between polls.
                sleeping(numberOfPolls = 100,
                        sleepTimeInMs = 10)
            }
        }

        //Specify a new callback dispatcher.
        //this dispatcher is responsible for
        //callbacks like success, fail and always.
        //it is expected that these callback do
        //very little work and never block
        callbackContext.dispatcher = buildDispatcher {
            name = "Tank"
            numberOfThreads = 1
        }

        // route internal errors when invoking
        //callbacks. This is also the place to
        //route this to a preferred logging
        //framework
        callbackContext.errorHandler =
                fun (e: Exception): Unit
                        = e.printStackTrace(System.err)

        // when promises are being resolved
        // multiple time, which is misuse of
        // the api this method is fired. You
        // can for instance choose to throw
        // an Exception here
        multipleCompletion =
                fun (a: Any, b: Any): Unit
                        = System.err.println(
                        "Tried resolving with $b, but is $a")
    }
}

