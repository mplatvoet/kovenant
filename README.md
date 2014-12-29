[![Circle CI](https://circleci.com/gh/mplatvoet/promises.svg?style=svg)](https://circleci.com/gh/mplatvoet/promises)

#Promises 0.3
A Promises implementation written in [Kotlin](http://kotlinlang.org) inspired by various Promises implementations.

Developed with the following goals in mind.

* **No configuration**: yet still able to modify 
* **Memory efficient**: trying to reduce the overhead as much as possible
* **Non blocking**: besides being thread safe everything should be non blocking
* **Dependency free**: when not counting kotlin std 


##Code samples

### Basic example
Running some code asynchronously is fairly simple. Just call `Promises.async {..}` with some code to execute and you get a Promise in return. 
If the operation is successful, meaning if no exception is thrown, the promise is considered successful. Otherwise the promise has failed. You can use `always` to get notified when to promise has been competed, no matter what the result is.
*Note that the order of calling `success`, `failed` and `always` is undefined.*

```kotlin
val promise = Promises.async {
	//some (long running) operation, or just:
	1 + 1
}

promise.success {
	//called when no exceptions have occurred
	println("result: $it")	
}

promise.failed {
	//called when an exceptions has occurred
	println("that's weird ${it.message}") 
}

promise.always {
	//no matter what result we get, this is always called once.
}
```

###Chaining
Naturally, the previous example can be written without those intermediate variables

```kotlin
Promises.async {
	//some (long running) operation, or just:
	1 + 1
} .success {
	//called when no exceptions have occurred
	println("result: $it")	
} .failed {
	//called when an exceptions has occurred
	println("that's weird ${it.message}") 
} .always {
	//no matter what result we get, this is always called once.
}
```


###Multiple Success stories
You don't have to limit yourself to registering just one callback. You can add multiple `success`, `failed` and `always` actions to one single promise. Thus a promise can be passed around and anybody who's interested can get notified. Previously registered callbacks don't get overwritten. Every callback will be called once and only once upon completion. The order of invocation is yet again undefined.

```kotlin
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

###Wait for it
Behind the scenes the default configuration creates a thread pool on which all operations are executed. This pool consists of solely non daemon threads, meaning the pool will shutdown when all other daemon threads are shutdown. *Therefor when the JVM shuts down, not all operations are guaranteed to complete!* More on this in the configuration section. 

There is a simple way to wait for completion, namely `Promises.await(vararg p:Promise<*>)`

##Configuring


###Executing
The default configuration leverages a thread pool with a number of threads equal to the number of processors. This pool consists of non daemon threads and shuts down when all other daemon threads are finished. So you might want to take matters in your own hand and take control over the executor. This can be achieved like this:
```kotlin
Promises.config {
	executor = MyExecutor() // 
}
```
*the default pool is lazy loaded, so no threads were wasted during config*




