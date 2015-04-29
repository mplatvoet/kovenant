#Then
part of [`kovenant-core`](../index.md#artifacts)

---

`then` operates similar to [`async`](async.md) except that it takes the output from a previous `Promise` as it's input.
This allows you to chain units of work.

```kt
async {
    fib(20)
} then {
    "fib(20) = $it, and fib(21) = (${fib(21)})"
} success {
    println(it)
}
```
Any `Exception` thrown from any of the steps in the chain of promises results in every next promises to be resolved as
failed. The work of `then` is executed by the worker `Dispatcher`. 

