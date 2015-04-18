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
By default the order of calling `success`, `fail` and `always` is in order of addition per callback type. So if 
 multiple `success` callbacks are added they will be executed in that same order. There is however no guarantee 
 for the order between different types of callbacks. So `always` could be executed before or after `success`. 
 
Please note that this is the default behaviour but can easily be broken. This is because Kovenant offers all the 
callbacks to Dispatcher in order, but depending on the configured Dispatcher the order can change. For instance,
if you configure the callbackDispatcher to operate with 2 threads the above behaviour will be broken. 
