#Callbacks

##The bare basics
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

##Chaining
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


##Multiple Success stories
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

##Execution order
The order of execution of the callbacks depends greatly on the underlying callback `Dispatcher`. Kovenant guarantees
that callbacks are offered to the `Dispatcher` in the same order they were added to the `Promise`. The default
callback Dispatcher also maintains this order. So by default all callbacks are executed in the same order they were 
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
So don't just blindly change the callback `Dispatcher` without actually understanding what you are doing.
