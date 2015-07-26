#Functional
part of [`kovenant-functional`](../index.md#artifacts)

---

This package adds common functional programming idioms to Kovenant. 
  
---

##map

Exactly the same as [`then`](core_usage.md#then) but simply renamed to map. So see [`then`](core_usage.md#then) for more 
information on usage.
  
---

##flatMap
`flatMap` operates similar to [`map`](#map) except that it expects a `bind` function that returns a promise and maps 
that as the result of this operation.

```kt
Promise.of(13).flatMap {
    divide(it, 12)
} success {
    println("Success: $it")
} fail {
    println("Fail: ${it.getMessage()}")
}

fun divide(a: Int, b: Int): Promise<Int, Exception> {
    return if (a == 0 || b == 0) {
        Promise.ofFail(Exception("Cannot divide by zero: $a/$b"))
    } else {
        Promise.ofSuccess(a / b)
    }
}
```
Any `Exception` thrown from any of the steps in the chain of promises results in every next promises to be resolved as
failed. The `bind` of `flatMap` is executed by the `workerContext`. 
  
---

##unwrap
Unwraps any nested Promise. By default the returned `Promise` will operate on the same `Context` as its parent
`Promise`, no matter what the `Context` of the nested `Promise` is. If you want the resulting promise to operate on
a different `Context` you can provide one.

Function tries to be as efficient as possible in cases where this or the nested `Promise` is already resolved. This
means that this function might or might not create a new `Promise`, it all depends on the current state.

```kt
val nested = Promise.of(Promise.of(42))
val promise = nested.unwrap()
promise success {
    println(it)
}
```

##withContext
Returns a `Promise` operating on the provided `Context`. This function might return the same instance of the `Promise` 
or a new one depending whether the `Context` of the `Promise` and the provided `Promise` match.

```kt
val p = Promise.of(42).withContext(Kovenant.context)
```


