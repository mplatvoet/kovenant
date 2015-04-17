[![Circle CI](https://circleci.com/gh/mplatvoet/kovenant.svg?style=svg&circle-token=fc8b76ad0630c6794673f67e65df3928b4a5ab86)](https://circleci.com/gh/mplatvoet/kovenant)

#Kovenant 0.1.0-kotlin.M11
A Promises implementation written in [Kotlin](http://kotlinlang.org) inspired by various Promises implementations.

```kt
val promise = async {
	//some (long running) operation, or just:
	1 + 1
}

promise success {
	//called when no exceptions have occurred
	println("result: $it")	
}
```

Please refer to the [Kovenant](http://kovenant.mplatvoet.nl) site for API usage and more.
 
##Quick start

###Maven
_Intended to be available via maven central_

###Gradle
_Intended to be available via maven central_

