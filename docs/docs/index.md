[![Circle CI](https://circleci.com/gh/mplatvoet/promises.svg?style=svg&circle-token=57cc98ef2342222e101c36dc06c1835f5954e8ce)](https://circleci.com/gh/mplatvoet/promises)

#Promises 0.3
A Promises implementation written in [Kotlin](http://kotlinlang.org) inspired by various Promises implementations.

Developed with the following goals in mind.

* **Easy to use**: familiar API and sensible defaults
* **Runtime agnostic**: API layer must be pure Kotlin
* **Memory efficient**: trying to reduce the overhead as much as possible
* **Non blocking**: besides being thread safe everything should be non blocking (JVM)
* **Dependency free**: when not counting kotlin std 



##Configuring


###Executing
The default configuration leverages a thread pool with a number of threads equal to the number of processors. This pool consists of non daemon threads and shuts down when all other daemon threads are finished. So you might want to take matters in your own hand and take control over the executor. This can be achieved like this:
```kotlin
Promises.config {
	executor = MyExecutor() // 
}
```
*the default pool is lazy loaded, so no threads were wasted during config*




