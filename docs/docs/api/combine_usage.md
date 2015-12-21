#Combine
part of [`kovenant-combine`](../index.md#artifacts)

---

A common case when working with promises is that you want to do something with the results of multiple promises
at once. So it would be nice to get notified when those promises are done and do things with the results right away.
This is what `combine` does. It takes from 2 till 20 (arbitrarily chosen) promises and combines them into one new Promise
that resolves, when successful, with a `Tuple` of all results combined. The first of any promise to fail makes the whole
promise fail. 
  

```kt
val fib20Promise = task { fib(20) }
val helloWorldPromise = task { "hello world" }

combine(fib20Promise, helloWorldPromise) success {
    val (fib, msg) = it
    println("$msg, fib(20) = $fib")
}
```
##and
For the special case of `combine` with only two parameters there is also an extension method `and` available. This
simply creates nicer looking code and plays well with conciseness fetishists:
```kt
task { fib(20) } and task { "hello world" } success {
    println("${it.second}, fib(20) = ${it.first}")
}
```