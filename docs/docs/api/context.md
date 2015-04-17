#Context
The `Context` object basically the current configuration. It can be obtained from `Kovenant.context` and configured
by `Kovenant.configure{...}`. Refer to the [configuration](configuration.md) section for the options. To create 
a completely new `Context` just use `Kovenant.createContext {...}` which uses the exact same options as `Kovenant.configure{...}`.

Functions like [`deferred`](deferred.md), [`async`](async.md) and [`then`](then.md) all have a first parameter which
is actually a `Context` instance. By default this is `Kovenant.context` so normally you don't have worry about this.
Just for that case you want to work with multiple configurations at once you have the possibility.

```kt
fun main(args: Array<String>) {
    val ctx = Kovenant.createContext {
        callbackDispatcher = buildDispatcher { name = "cb-new" }
        workerDispatcher = buildDispatcher { name = "work-new" }
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
