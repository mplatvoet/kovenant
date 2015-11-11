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

There are cases where you want to control the number of parallel tasks in certain parts of your application, but not all.
For instance when some tasks use up large amounts of memory. This is where a `Throttle` comes in to play. With a 
 `Throttle` you're able to configure how many tasks are allowed to run in parallel. A `Throttle` can be used over 
multiple existing Dispatchers, so you don't have to worry on which context Promises run.

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
  
