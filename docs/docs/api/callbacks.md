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

[*Note that the order of calling `success`, `fail` and `always` is undefined.*](#execution-order)

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
The order of calling `success`, `fail` and `always` is *undefined* by design. This means
that `always` can be fired before `success`, but this can just as easily be the other way around. Hence, even
multiple `success` callbacks are *not* guaranteed to be executed in the order they where added. 
