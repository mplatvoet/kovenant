[![CircleCI branch](https://img.shields.io/circleci/project/mplatvoet/kovenant/master.svg)](https://circleci.com/gh/mplatvoet/kovenant/tree/master) [![Maven Central](https://img.shields.io/maven-central/v/nl.mplatvoet.komponents/kovenant.svg)](http://search.maven.org/#browse%7C-339523586) [![DUB](https://img.shields.io/dub/l/vibe-d.svg)](https://github.com/mplatvoet/kovenant/blob/master/LICENSE)

#Kovenant
[Promises](http://en.wikipedia.org/wiki/Futures_and_promises) for [Kotlin](http://kotlinlang.org)

```kt
async { "world" } and async { "Hello" } success {
    println("${it.second} ${it.first}!")
}
```

Please refer to the [Kovenant](http://kovenant.mplatvoet.nl) site for API usage and more.
 
## Getting started
This version is build against `Java 8` and `kotlin-stdlib:0.11.91.2`.
Source and target compatibility is `Java 6`

###Gradle
```groovy
dependencies {
    compile 'nl.mplatvoet.komponents:kovenant:1.1.+'
}
```

###Maven
```xml
<dependency>
	<groupId>nl.mplatvoet.komponents</groupId>
	<artifactId>kovenant</artifactId>
	<version>[1.1.0,1.2.0)</version>
</dependency>
```

###Artifacts
Kovenant has been structured in sub projects so you can cherry pick what you need. Especially for Android
it's needed to keep class and method count low. 

|artifact          |description                                                                                        |
|------------------|---------------------------------------------------------------------------------------------------|
|kovenant          |Container artifact that consists of `kovenant-core`, `kovenant-combine` and `kovenant-jvm`         |
|kovenant-core     |The core of kovenant. Provides the API and default implementations                                 |
|kovenant-combine  |Adds combine functionality that keep everything strongly typed                                     |
|kovenant-jvm      |Support for converting between Executors and Dispatchers                                           |
|kovenant-android  |Extensions for Android specific needs                                                              | 

##Issues
Issues are tracked in [Youtrack](http://komponents.myjetbrains.com/youtrack/issues?q=project%3A+Kovenant)