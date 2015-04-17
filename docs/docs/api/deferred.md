#Deferred
With a `Deferred<V,E>` you can take matters in your own hand. Instead of relying on [`async`](async.md) for resolving
or rejecting a promise it's up to the developer. You obtain a deferred object by simply calling `deferred<V, E>()`.
From there you can either `resolve(V)` or `reject(E)` the deferred. This can only be set once and by default trying
to resolve or reject multiple times throws an Exception. The behaviour can be [configured](configuration.md) though.
 
From a `Deferred<V,E>` we can obtain the companion `Promise<V, E>` as easy as `deferred.promise`. This promise can
of course passed around as much as you want, just like any promise. Just keep the deferred to yourself. 

##Example

```kt
fun foo() {
    val deferred = deferred<String,Exception>()
    handlePromise(deferred.promise)
    
    deferred.resolve("Hello World")
//    deferred.reject(Exception("Hello exceptional World"))
}
fun handlePromise(promise: Promise<String, Exception>) {
    promise success {
        msg -> println(msg)
    }
    promise fail {
        e -> println(e)
    }
}
```