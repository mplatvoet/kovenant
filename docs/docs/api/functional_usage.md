#Functional
part of [`kovenant-functional`](../index.md#artifacts)

---

This package adds common functional programming idioms to Kovenant. 
  
---

##map

Exactly the same as [`then`](core_usage.md#then) but simply renamed to map. So see [`then`](core_usage.md#then) for more 
information on usage.
  
---

##bind
`bind` operates similar to [`map`](#map) except that it expects a `fn` function that returns a promise and maps 
that as the result of this operation.

```kt
Promise.of(13) bind {
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
failed. The `fn` of `bind` is executed by the `workerContext`. 
  
---

##apply
Applies the map function of the provided `Promise` to the result of this `Promise` and returns a new `Promise` with
the transformed value.

If either this or the provided `Promise` fails the resulting `Promise` has failed too. this `Promise` takes
precedence over the provided `Promise` if both fail.

```kt
val p = Promise.of(21) apply Promise.of({ x: Int -> x * 2 })
p success { println(it) }
```


