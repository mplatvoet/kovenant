#Kovenant Core Configuration
part of [`kovenant-core`](../index.md#artifacts)

---

##Context
The `Context` object is basically the current configuration. It can be obtained from `Kovenant.context` and configured
by `Kovenant.configure{...}`. Refer to the [configuration](#configuration) section for the options. To create 
a completely new `Context` just use `Kovenant.createContext {...}` which uses the exact same options as `Kovenant.configure{...}`.

Functions like [`deferred`](core_usage.md#deferred) and [`async`](core_usage.md#async) have a first parameter which
is actually a `Context` instance. By default this is `Kovenant.context` so normally you don't have worry about this.
Just for that case you want to work with multiple configurations at once you have the possibility.
 

```kt
fun main(args: Array<String>) {
    val ctx = Kovenant.createContext {
        callbackContext.dispatcher = buildDispatcher { name = "cb-new" }
        workerContext.dispatcher = buildDispatcher { name = "work-new" }
    }

    async {
        println("default async $threadName")
    } success {
        println("default success $threadName")
    }

    async(ctx) {
        println("ctx async $threadName")
    } success {
        println("ctx success $threadName")
    }
}

private val threadName : String get() = Thread.currentThread().getName()
```

##Configuration
Configuration of Kovenant is done entirely in code and any changes to the [`Context`](#context) are completely 
threadsafe, so Kovenant can be reconfigured during a running application from multiple threads. But you probably want 
to do this when your application starts. 

Configuring is done by simply calling `Kovenant.configure { ... }`. 

###Dispatchers
Kovenant operates with two `Dispatcher`s, a worker and callback `Dispatcher`. They are configured as follows:

```kt
Kovenant.configure {
    workerContext.dispatcher = ...
    // or
    callbackContext {
        dispatcher = ...
    }
}
```
You can provide your own `Dispatcher` implementation, [convert](jvm_usage.md) an existing `Executor` (Jvm) or 
use `buildDispatcher` (Jvm) to create configure the build in `Dispatcher`.

>CONFIGURE **ONE** THREAD FOR THE CALLBACK DISPATCHER
>understand the [implications](core_usage.md#execution-order) when using more then one thread

####buildDispatcher
Let me state upfront that this method is *not threadsafe*.

```kt
buildDispatcher {
    name = "Dispatcher Name"
    numberOfThreads = 1 
    exceptionHandler = ...// (Exception) -> Unit
    errorHandler = ... // (Throwable) -> Unit
    configurePollStrategy { ... }
}
```
**name**
Sets the name of this `Dispatcher`. Is also used as thread names appended by a number

**numberOfThreads**
The maximum number of threads this `Dispatcher` concurrently keeps alive. Note that the actual
number of threads can be lower and depends on how much work is offered. Also, during the lifetime
of the `Dispatcher` the number ov threads instantiated can be far greater because threads can 
also be destroyed.

**exceptionHandler**
Get notified of exceptions from within the `Dispatcher` of running tasks. Normally Kovenant handles errors on the 
Promise level but the Dispatcher can also be used directly and those jobs might leak exceptions.

**errorHandler**
When things go seriously wrong, e.g. `OutOfMemoryError`, this is what is tried to be called.

**configurePollStrategy**
The way you configure you poll strategy greatly influences the way the `Dispatcher` behaves. Poll strategies
can be chained and are executed in order of configuration. 

|strategy|parameters|description|
|--------|----------|-----------|
|yielding|numberOfPolls|Keeps polling the `numberOfPolls` and [yields](https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#yield()) the thread between polls. Yielding is most suitable on environments where the number of active threads exceeds the number of physical cores.|
|busy|numberOfPolls|Keeps polling the `numberOfPolls`. Use when a lot of work is expected very frequently (nano seconds)|
|blocking|<none>|Blocks until there is new work. *Note that this approach prevents the `Dispatcher` from shutting down*. This strategy only makes sense as last of the chain since it either receives work or blocks. Can also block the producing threads for short amounts of time so effectively kills the non blocking nature of Kovenant|
|sleeping|numberOfPolls, sleepTimeInMs|Sleeps in between the `numberOfPolls` for the given `sleepTimeInMs`. Doesn't wake earlier if new is presented but sleeps the whole thing through. It is thus advised to keep `sleepTimeMs` very low |
|blockingSleep|numberOfPolls,|sleepTimeInMs|Sleeps in between the `numberOfPolls` for the given `sleepTimeInMs`. Wakes up early if work is presented within the `sleepTimeInMs`. Can also block the producing threads for short amounts of time so effectively kills the non blocking nature of Kovenant|

```kt
val dispatcher = buildDispatcher {
    name = "Bob the builder"
    numberOfThreads = 1
    
    configurePollStrategy {                
        yielding(numberOfPolls = 1000)
        
        sleeping(numberOfPolls = 100, 
                     sleepTimeInMs = 10)
        blocking()
    }
}
```

What's best for your situation depends on your needs. So like always with concurrency: test instead of guess.

###Common example

```kt
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
```
