#Async
The easiest way to create a `Promise` is by using `async`, e.g.
```kt
val promise = async { foo() }
```
This will execute function `foo()` asynchronously and immediately returns a `Promise<V, Exception>` where `V` is
the inferred return type of `foo()`. If `foo()` completes successful the `Promise` is resolved as successful. Any 
`Exception` from `foo()` is considered a failure.

`async` dispatches the work on the [`workerDispatcher`](configuration.md). 