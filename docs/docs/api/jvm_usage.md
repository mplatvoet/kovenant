#JVM specific additions
part of [`kovenant-jvm`](../index.md#artifacts)

---

##Executors

It's likely that your project already uses threadpools or that you want to leverage the threadpool
Kovenant uses. Because too many threads just leads to context switching and thus performance degradation.
Therefore Kovenant provides some facilities for interoperability with Java's Executors. 

* To convert Kovenant's `Dispatcher`s to Java's `Executor` use the extension function `asExecutor()` 
* To convert Kovenant's `Dispatcher`s to Java's `ExecutorService` use the extension function `asExecutorService()`
* To convert Java's `Executor` or `ExecutorService` to Kovenant's `Dispatcher` use the extension function `asDispatcher()`


```kt
fun main(args: Array<String>) {
    val executorService = Kovenant.context.workerContext.dispatcher.asExecutorService()

    val tasks = listOf(*(Array(5) { FibCallable(25 - it) }))


    val (n, fib) = executorService.invokeAny(tasks)
    println("invokeAny: fib($n) = $fib")
    println()

    val results = executorService.invokeAll(tasks)
    results forEach { future ->
        val (i, res) = future.get()
        println("invokeAll: fib($i) = $res")
    }

    //Not necessary but shuts down a bit quicker
    executorService.shutdownNow()
}


private class FibCallable(private val n: Int) : Callable<Pair<Int, Int>> {
    override fun call() = Pair(n, fib(n))
}
```

---

##Throttle
There are case where finer control of the number of parallel tasks is needed, for instance when some specific type of task uses vast amounts of memory. This is what a `Throttle` is for. It allows you to limit the number of parallel task completely independent of any underlying `Dispatcher`. So no matter on what `Context` and thus `Dispatcher` your async tasks run, it's always within bounds. 

You create a `Throttle` instance simply calling its constructor with any positive number indicating the maximum number of concurrent tasks:

```kotlin
// a Throttle with 2 parallel tasks at max
val myThrottle = Throttle(2)
```

Optionally you can provide the `Context` on which new async tasks are run on by default. The can always be overridden per specific task. 

```kotlin
//configure with myContext
//the default is Kovenant.context
val myThrottle = Throttle(2, myContext)
```

###simple tasks
The easiest way to use a `Throttle` instance is by using the `task` method. This creates a task similar to the general `async` method that gets scheduled somewhere in the future. The result is, of course, a `Promise`

```kotlin
myThrottle.task {
     foo()
} always {
    println("done")
}
```

###manual registering
Sometimes you want to throttle a whole chain of promises. So you need to manually register the start and end of the chain. The `registerTask` and `registerDone` gives you that freedom. It's up to you to make sure that every `registerTask` is balanced with a countering `registerDone`. Failing to do so may either result in more than the configured tasks to run parallel or simply a deadlock.

```kotlin
val promise = myThrottle.registerTask { foo() }

//the rest of the chain
val lastPromise = promise then { bar() } then { baz() }

myThrottle.registerDone(lastPromise)
```

###Full Throttle example
```kt
fun main(args: Array<String>) {
    Kovenant.context {
        workerContext.dispatcher { concurrentTasks = 8 }
    }

    val sleepThrottle = Throttle(4)
    val snoozeThrottle = Throttle(2)

    val promises = (1..10).map { number ->
        sleepThrottle.task {
            Thread.sleep(1000)
            number
        } success {
            println("#$it sleeper awakes")
        }
    }

    promises.forEach { promise ->
        val registeredPromise = snoozeThrottle.registerTask(promise) {
            Thread.sleep(700)
        }

        val finalPromise = registeredPromise then {
            "#${promise.get()} snoozing"
        }

        snoozeThrottle.registerDone(finalPromise)

        finalPromise success {
            println(it)
        }

    }

    println("all tasks created")
}
```
  
