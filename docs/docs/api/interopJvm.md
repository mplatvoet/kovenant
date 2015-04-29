#Executors (JVM)
part of [`kovenant-core`](../index.md#artifacts)

---

It's likely that your project already uses threadpools or that you want to leverage the threadpool
Kovenant uses. Because to many threads just leads to context switching and thus performance degradation.
Therefor Kovenant provides some facilities for interoperability with Java's Executors. This is of course Jvm only
functionality. 

* To convert Kovenant's `Dispatcher`s to Java's `Executor` use the extension function `asExecutor()` 
* To convert Kovenant's `Dispatcher`s to Java's `ExecutorService` use the extension function `asExecutorService()`
* To convert Java's `Executor` or `ExecutorService` to Kovenant's `Dispatcher` use the extension function `asDispatcher()`


```kt
fun main(args: Array<String>) {
    val executorService = Kovenant.context.workerDispatcher.asExecutorService()

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