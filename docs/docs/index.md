[![CircleCI branch](https://img.shields.io/circleci/project/mplatvoet/kovenant/master.svg)](https://circleci.com/gh/mplatvoet/kovenant/tree/master) [![Maven Central](https://img.shields.io/maven-central/v/nl.komponents.kovenant/kovenant.svg)](http://search.maven.org/#browse%7C1069530195) [![DUB](https://img.shields.io/dub/l/vibe-d.svg)](https://github.com/mplatvoet/kovenant/blob/master/LICENSE)

#Kovenant
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

Developed with the following [goals](misc/goals.md) in mind.

* **Easy to use**: Function above anything else
* **Runtime agnostic**: API layer must be pure Kotlin
* **Memory efficient**: trying to reduce the overhead as much as possible
* **Non blocking**: besides being thread safe everything should be non blocking (JVM)
* **Dependency free**: when not counting kotlin std 

## Getting started
This version is build against `kotlin-stdlib:0.12.412`.
Source and target compatibility is `1.6`

###Gradle
```groovy
dependencies {
    compile 'nl.komponents.kovenant:kovenant:2.0.+'
}
```

###Maven
```xml
<dependency>
	<groupId>nl.komponents.kovenant</groupId>
	<artifactId>kovenant</artifactId>
	<version>[2.0.0,2.1.0)</version>
</dependency>
```

###Android Demo app
Checkout the [Android Demo App on Github](https://github.com/mplatvoet/kovenant-android-demo).

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
Issues are tracked in [Youtrack](http://issues.komponents.nl/youtrack/issues?q=project%3A+Kovenant)

##Release notes
See [Changelog](changelog.md) for release notes