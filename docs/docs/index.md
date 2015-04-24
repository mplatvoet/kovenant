[![Circle CI](https://circleci.com/gh/mplatvoet/kovenant.svg?style=svg&circle-token=fc8b76ad0630c6794673f67e65df3928b4a5ab86)](https://circleci.com/gh/mplatvoet/kovenant)

#Kovenant 0.1.0
[Promises](http://en.wikipedia.org/wiki/Futures_and_promises) for [Kotlin](http://kotlinlang.org)

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

Developed with the following [goals](goals.md) in mind.

* **Easy to use**: Function above anything else
* **Runtime agnostic**: API layer must be pure Kotlin
* **Memory efficient**: trying to reduce the overhead as much as possible
* **Non blocking**: besides being thread safe everything should be non blocking (JVM)
* **Dependency free**: when not counting kotlin std 

## Getting started

###Gradle
_Soon on maven central_

###Maven
_Soon on maven central_
