[![Circle CI](https://circleci.com/gh/mplatvoet/kovenant.svg?style=svg&circle-token=fc8b76ad0630c6794673f67e65df3928b4a5ab86)](https://circleci.com/gh/mplatvoet/kovenant)

#Kovenant 0.1.0-kotlin.M11
A Promises implementation written in [Kotlin](http://kotlinlang.org) inspired by various Promises implementations.

Developed with the following [goals](goals.md) in mind.

* **Easy to use**: Function above anything else
* **Runtime agnostic**: API layer must be pure Kotlin
* **Memory efficient**: trying to reduce the overhead as much as possible
* **Non blocking**: besides being thread safe everything should be non blocking (JVM)
* **Dependency free**: when not counting kotlin std 

## Basic example
Running some code asynchronously is fairly simple. Just call `async {..}` with some code to execute and you'll get a `Promise` in return. 
If the operation is successful, meaning if no exception is thrown, the promise is considered successful. Otherwise the promise has failed. You can use `always` to get notified when to promise has been completed, no matter what the result is.

```kt
async {
	//some (long running) operation, or just:
	1 + 1
} then { 
	i -> "result: $i"	
} success { 
	msg -> println(msg)
}
```

## Getting started

###Gradle
Soon to be released on maven central

###Maven
Soon to be released on maven central
