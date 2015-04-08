[![Circle CI](https://circleci.com/gh/mplatvoet/kovenant.svg?style=svg&circle-token=fc8b76ad0630c6794673f67e65df3928b4a5ab86)](https://circleci.com/gh/mplatvoet/kovenant)

#Kovenant 0.1.0
A Promises implementation written in [Kotlin](http://kotlinlang.org) inspired by various Promises implementations.

Developed with the following goals in mind.

* **Easy to use**: familiar API and sensible defaults
* **Runtime agnostic**: API layer must be pure Kotlin
* **Memory efficient**: trying to reduce the overhead as much as possible
* **Non blocking**: besides being thread safe everything should be non blocking (JVM)
* **Dependency free**: when not counting kotlin std 


##Code samples

### Basic example
Running some code asynchronously is fairly simple. Just call `Kovenant.async {..}` with some code to execute and you'll get a `Promise` in return. 
If the operation is successful, meaning if no exception is thrown, the promise is considered successful. Otherwise the promise has failed. You can use `always` to get notified when to promise has been competed, no matter what the result is.
*Note that the order of calling `success`, `failed` and `always` is undefined.*

```kotlin
val promise = Kovenant.async {
	//some (long running) operation, or just:
	1 + 1
}

promise.success {
	//called when no exceptions have occurred
	println("result: $it")	
}
```

###And `then`...
Naturally, the previous example can be written without those intermediate variables

```kotlin
Kovenant.async {
	//some (long running) operation, or just:
	1 + 1
} .success {
	//called when no exceptions have occurred
	println("result: $it")	
} 
```

Kovenant also provides a `then` function in order to chain units of work 

```kotlin
Kovenant.async {
	//some (long running) operation, or just:
	1 + 1
} .then { i ->
	"result: $i"	
} .success { msg ->
	println(msg)
}
```


###Multiple Success stories
You don't have to limit yourself to registering just one callback. You can add multiple `success`, `failed` and `always` actions to one single promise. Thus a promise can be passed around and anybody who's interested can get notified. Previously registered callbacks don't get overwritten. Every callback will be called once and only once upon completion. The order of invocation is yet again undefined.

```kotlin
Kovenant.async {
	1 + 1
} .success {
	println("1")	
} .success {
	println("2")	
} .success {
	println("3")	
}
```

##Concurrency (JVM)
Although Kovenant hides a lot of implementation details it's important to know what is happening behind the scenes. 
Kovenant uses two thread pools, a worker pool and a dispatcher pool. `success`, `fail` and `always` are executed by the
dispatcher pool. `async` and `then` are executed on the worker pool.

The default pool shuts down threads when there isn't any work left in their queues. So normally you don't have to worry 
about shutting down any pools since they will do that themselves. It is important to realize though that because of this
behaviour short spurts of work on the promises will spawn new threads and shut them down constantly. 
This behaviour can be avoided in the config section, though you need to shutdown the pools manually.   



##Configuring


###Executing
The default configuration leverages a thread pool with a number of threads equal to the number of processors. This pool consists of non daemon threads and shuts down when all other daemon threads are finished. So you might want to take matters in your own hand and take control over the executor. This can be achieved like this:
```kotlin
Kovenant.configure { 
	callbackDispatcher = MyDispatcher()
	workerDispatcher = MyDispatcher()
}
```
*the default pool is lazy loaded, so no threads were wasted during config*




