#Kovenant Core Usage
part of [`kovenant-core`](../index.md#artifacts)

---

##Async
The easiest way to create a `Promise` is by using `async`, e.g.
```kt
val promise = async { foo() }
```
This will execute function `foo()` asynchronously and immediately returns a `Promise<V, Exception>` where `V` is
the inferred return type of `foo()`. If `foo()` completes successful the `Promise` is resolved as successful. Any 
`Exception` from `foo()` is considered a failure.

`async` dispatches the work on the [`workerContext`](core_config.md). 

---

##Deferred
With a `Deferred<V,E>` you can take matters in your own hand. Instead of relying on [`async`](#async) for resolving
or rejecting a promise it's up to the developer. You obtain a deferred object by simply calling `deferred<V, E>()`.
From there you can either `resolve(V)` or `reject(E)` the deferred. This can only be set once and by default trying
to resolve or reject multiple times throws an Exception. The behaviour can be [configured](core_config.md) though.
 
From a `Deferred<V,E>` we can obtain the companion `Promise<V, E>` as easy as `deferred.promise`. This promise can
of course passed around as much as you want, just like any promise. Just keep the deferred to yourself. 

###Example

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

---

##Callbacks
A `Promise<V, E>` allows you to add 3 types of callbacks:

* `success` of type `(V) -> Unit`
* `fail` of type `(E) -> Unit`
* `always` of type `() -> Unit`

When a promise resolves successfully all current registered `success` callbacks are fired. Of course, any
new callback will be fired as well. If the promise has failed all the current registered `fail` callbacks will
be fired. And again, any new callback will be fired as well. 

No matter what the result of the promise is, success or failure, `always` get fired always upon completion.

[*Please read the extra note on the order of calling `success`, `fail` and `always`.*](#execution-order)

```kt
val promise = async {
	//mimicking those long running operations with:
	1 + 1
}

promise success {
	//called on succesfull completion of the promise
	println("result: $it")	
}

promise fail {
	//called when an exceptions has occurred
	println("that's weird ${it.message}") 
}

promise always {
	//no matter what result we get, this is always called once.
}
```

###Chaining
All callback registering functions return `this` Promise, thus previous example can be written without those intermediate variables

```kt
async {
	//some (long running) operation, or just:
	1 + 1
} success {
	//called when no exceptions have occurred
	println("result: $it")	
} fail {
	//called when an exceptions has occurred
	println("that's weird ${it.message}") 
} always {
	//no matter what result we get, this is always called once.
}
```

###DispatcherContext
By default the callbacks are executed on the callback `DispatcherContext` that is associated with this `Promise`. 
But you can also provide your own DispatcherContext for a specific callback.

```kt
val dispatcherContext = //...

async {
	foo()
}.success(dispatcherContext) {
	bar()
}

```


###Multiple Success stories
You don't have to limit yourself to registering just one callback. You can add multiple `success`, `fail` and `always` actions to one single promise. 
Thus a promise can be passed around and anybody who's interested can get notified. Previously registered callbacks don't get overwritten. 
Every callback will be called once and only once upon completion.

```kt
async {
	1 + 1
} success {
	println("1")	
} success {
	println("2")	
} success {
	println("3")	
}
```

###Execution order
The order of execution of the callbacks depends greatly on the underlying callback `DispatcherContext`. Kovenant guarantees
that callbacks are offered to the `DispatcherContext` in the same order they were added to the `Promise`. The default
callback DispatcherContext also maintains this order. So by default all callbacks are executed in the same order they were 
added. 

The default behaviour can easily be broken though. For instance, if you configure the callbackDispatcher to operate with 
2 threads the order of execution becomes undefined. Not knowing the order of execution can have some undesired side 
 effects. For example, consider:
```kt
val firstRef = AtomicReference<String>()
val secondRef = AtomicReference<String>()

val first = async { "hello" } success {
	firstRef.set(it)
}
val second = async { "world" } success {
	secondRef.set(it)
}

all (first, second) success {
	println("${firstRef.get()} ${secondRef.get()}")
}
```

If we don't have guarantees about the order of the callbacks the above example simply won't work. This is because
the `all` function also relies on callbacks on the `first` and `second` promise. So without order guarantees the
`success`callback of `all` might just execute before the `success` callbacks of the `first` and `second` promise. 
So don't just blindly change the callback `DispatcherContext` without actually understanding what you are doing.

---

##Then
`then` operates similar to [`async`](#async) except that it takes the output from a previous `Promise` as its input.
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
failed. The work of `then` is executed by the `workerContext`. 

---

##Then Use
`thenUse` operates similar to [`then`](#then) except that it takes the output from a previous `Promise` as its input
as an extension function. The previous example would thus be:

```kt
async {
    fib(20)
} thenUse {
    "fib(20) = $this, and fib(21) = (${fib(21)})"
} success {
    println(it)
}
```

---

##Get
Sometimes you need to jsut wait for a result, this is what `get()` does. It blocks the calling thread until the result 
is available. Returning the success value if resolved successful. If promise resolved as a failure an Exception is thrown.
If the error value of the promise is an Exception then that is thrown directly, otherwise a FailureException is thrown
with the error value wrapped in it.

```kt
val fib20 : Int = async { fib(20) }.get()
```
>Note that implementors should override this function because the fallback methods are far from efficient

---

##isDone
The functions `isDone()`, `isFailure()` and `isSuccess()` simply tells you if this promise is resolved and whether
it is successful or has failed. This comes in handy in combination with `get()`. 


>Note that implementors should override these functions because the fallback functions are far from efficient

---

##Lazy Promise
Kovenant provides a `lazyPromise` property delegate similar to Kotlin's standard library `Delegates.lazy {}`. 
The difference with the standard library version is that initialization happens by an [`async`](#async) operation and
thus effectively on a background thread. This is particularly useful on platforms like Android where you want to avoid
initialization on the UI Thread. 

```kt
val expensiveResource by lazyPromise {
    println("init promise")
    ExpensiveResource()
}

fun main(args: Array<String>) {
    println("start program")

    expensiveResource thenUse {
        "Got [$value]"
    } success {
        println(it)
    }
}


class ExpensiveResource {
    val value :String = "result"
}
```

---

##Of
In order align with existing libraries and code you can create promises of existing values with `of`, `ofSuccess` 
and `ofFail`.
 
```kt
// Success promise with inferred value type
// and Exception as fail type 
Promise.of(13)

// Failed promise with explicit types
Promise.ofFail<String, Int>(13)

// Successful promise with explicit types
Promise.ofSuccess<String, Int>("thirteen")
```

---

##All
Sometimes you want to make sure that multiple promises are done before proceeding. With `all` this can be achieved.
`all<V,Exception>` takes a `vararg` of `Promise<V,Exception>`s and returns a `Promise<List<V>, Exception>`. 
The returned `Promise` is considered a success if all of the provided `Promise`s are successful. If any fail the whole 
promise fails. The returned `List<V>` contains the items in the same order as the `Promise`s provided to `all`. 
If you want to mix promises of different types you probably want to take a look at [combine](combine_usage.md)

By default `all` tries to cancel all provided promises if one fails. If you don't want your promises to be cancelled
you can set `cancelOthersOnFail = false`. See [cancel](#cancel) for more on this topic.

```kt
val promises = Array(10) { n ->
	async {
		Pair(n, fib(n))
	}
}

all(*promises) success {
	it forEach { pair -> println("fib(${pair.first}) = ${pair.second}") }
} always {
	println("All ${promises.size()} promises are done.")
}
```


---

##Any
Sometimes you want to make sure that at least one of multiple promises is done before proceeding. With `any` this can be achieved.
`any<V,Exception>` takes a `vararg` of `Promise<V,Exception>`s and returns a `Promise<V, List<Exception>>`. The returned `Promise` is considered a
success if any of the provided `Promise`s is successful. If all fail the whole promise fails. The returned `List<Exception>`
contains the items in the same order as the `Promise`s provided to `any`.

By default `any` tries to cancel all provided promises if one succeeds. If you don't want your promises to be cancelled
you can set `cancelOthersOnSuccess = false`. See [cancel](#cancel) for more on this topic.

```kt
val promises = Array(10) { n ->
	async {
		while(!Thread.currentThread().isInterrupted()) {
			val luckyNumber = Random(System.currentTimeMillis() * n).nextInt(100)
			if (luckyNumber == 7) break
		}
		"Promise number $n won!"
	}
}

any (*promises) success { msg ->
	println(msg)
	println()

	promises forEachIndexed { n, p ->
		p.fail { println("promise[$n] was canceled") }
		p.success { println("promise[$n] finished") }
	}
} 
```

---

##Cancel
Any `Promise` that implements `CancelablePromise` allows itself to be cancelled. By default the promises returned
from [`async`](#async), [`then`](#then) and [`thenUse`](#thenUse) are `CancelablePromise`s.

Cancelling a promises is quite similar to [`Deferred.reject`](#deferred) as it finishes the promises as failed. Thus
the callbacks `fail` and `always` are still executed. Cancel does also try to prevent the promised work from ever being 
 scheduled. If the promised work is already running it gets [interrupted](https://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#interrupt()) (when using default dispatchers).