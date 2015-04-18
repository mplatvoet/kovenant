#Combine
A common case when working with promises is that you want to do something with the results of multiple promises
at once. So it would be nice to get notified when those promises are done and do things with the results right away.
This is what `combine` does. It takes from 2 till 20 (arbitrarily chosen) promises and combines them into one new Promise
that resolves, when successful, with a `Tuple` of all results combined. The first of any promise to fail makes the whole
promise fail. 
  
For the special case of `combine` with only two parameters there is also an extension method `and` available. This
simply creates nice looking code:
```kt
val fib20Promise = async { fib(20) }
val helloWorldPromise = async { "hello world" }

fib20Promise and helloWorldPromise success {
    val (fib, msg) = it
    println("$msg, fib(20) = $fib")
}
```