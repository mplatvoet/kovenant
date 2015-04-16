#Callbacks

##The bare basics
A `Promise<V, E>` allows you to add try types of callbacks:

* `success` of type `(V) -> Unit`
* `fail` of type `(E) -> Unit`
* `always` of type `() -> Unit`


*Note that the order of calling `success`, `failed` and `always` is undefined.*

```kt
val promise = Kovenant.async {
	//mimicing those long running ops with:
	1 + 1
}

promise.success {
	//called on succesfull completion of the promise
	println("result: $it")	
}

promise.fail {
	//called when an exceptions has occurred
	println("that's weird ${it.message}") 
}

promise.always {
	//no matter what result we get, this is always called once.
}
```

##Chaining
Naturally, the previous example can be written without those intermediate variables

```kt
Kovenant.async {
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
You don't have to limit yourself to registering just one callback. You can add multiple `success`, `fail` and `always` actions to one single promise. Thus a promise can be passed around and anybody who's interested can get notified. Previously registered callbacks don't get overwritten. Every callback will be called once and only once upon completion. The order of invocation is yet again undefined.

```kt
Promises.async {
	1 + 1
} .success {
	println("1")	
} .success {
	println("2")	
} .success {
	println("3")	
}
```

