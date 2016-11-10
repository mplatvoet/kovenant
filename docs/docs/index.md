[![CircleCI branch](https://img.shields.io/circleci/project/mplatvoet/kovenant/master.svg)](https://circleci.com/gh/mplatvoet/kovenant/tree/master) [![Maven Central](https://img.shields.io/maven-central/v/nl.komponents.kovenant/kovenant.svg)](http://search.maven.org/#browse%7C1069530195) [![DUB](https://img.shields.io/dub/l/vibe-d.svg)](https://github.com/mplatvoet/kovenant/blob/master/LICENSE.txt)

#Kovenant
[Promises](http://en.wikipedia.org/wiki/Futures_and_promises) for [Kotlin](http://kotlinlang.org)

The easy asynchronous library for Kotlin. With extensions for Android, RxJava, JavaFX and much more.

```kt
task {
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
Build against Kotlin: `1.0.3`.
Source and target compatibility is `1.6`

###Gradle
```groovy
dependencies {
    compile 'nl.komponents.kovenant:kovenant:3.3.0'
}
```

###Maven
```xml
<dependency>
	<groupId>nl.komponents.kovenant</groupId>
	<artifactId>kovenant</artifactId>
	<version>3.3.0</version>
	<type>pom</type>
</dependency>
```

###Android Demo app
Checkout the [Android Demo App on Github](https://github.com/mplatvoet/kovenant-android-demo).

###Artifacts
Kovenant has been structured in sub projects so you can cherry pick what you need. 

|artifact            |description                                                                                        |
|--------------------|---------------------------------------------------------------------------------------------------|
|kovenant            |Container artifact that consists of `kovenant-core`, `kovenant-combine`, `kovenant-jvm` and `kovenant-functional`|
|kovenant-core       |The core of kovenant. Provides the API and default implementations                                 |
|kovenant-combine    |Adds combine functionality that keep everything strongly typed                                     |
|kovenant-jvm        |Support for converting between Executors and Dispatchers                                           |
|kovenant-ui         |Support for UI frameworks that need UI work to operate on a specific process                       |
|kovenant-rx         |Add promise support to Rx                                                                          |
|kovenant-android    |Extensions for Android specific needs                                                              |
|kovenant-jfx        |Extensions for JavaFX specific needs                                                               | 
|kovenant-disruptor  |LMAX Disruptor work queues                                                                         | 
|kovenant-progress   |Progress configuration helper                                                                      | 
|kovenant-functional |Functional Programming idiomatic additions                                                         | 

##Issues
Issues are tracked in [Youtrack](http://issues.komponents.nl/youtrack/issues?q=project%3A+Kovenant)

##Release notes
See [Changelog](changelog.md) for release notes

##Slack
Join the `#kovenant` channel on [Kotlin Slack](http://kotlinslackin.herokuapp.com). 

## More Kotlin libraries
Check out [Awesome Kotlin](https://github.com/JavaBy/awesome-kotlin)
