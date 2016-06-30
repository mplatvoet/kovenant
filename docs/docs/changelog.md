#Changelog

Changelog of Kovenant. Complying to [Semantic Versioning](http://semver.org).
Please refer to [roadmap](roadmap.md) for upcoming releases.

##v3.3.0

**general**

* Update to Kotlin 1.0.3
* [KOV-80](http://issues.komponents.nl/youtrack/issue/KOV-80) enhance/correct the documentation

**core**

* [KOV-87](http://issues.komponents.nl/youtrack/issue/KOV-87) Cancelable deferred


##v3.2.2

**general**

* Update to Kotlin 1.0.1-2

##v3.2.1

**general**

* Update to Kotlin 1.0.1-1

**core**

* [KOV-78](http://issues.komponents.nl/youtrack/issue/KOV-78) Support android with api level set to 15

##v3.2.0

**core**

* [KOV-74](http://issues.komponents.nl/youtrack/issue/KOV-74) Move `unwrap` to core library
* [KOV-75](http://issues.komponents.nl/youtrack/issue/KOV-75) Move `withContext` to core library
* [KOV-76](http://issues.komponents.nl/youtrack/issue/KOV-76) rename `thenUse` to `thenApply`

**general**

* [KOV-77](http://issues.komponents.nl/youtrack/issue/KOV-77) Kotlin 1.0.1


##v3.1.0

**rx**

* [KOV-68](http://issues.komponents.nl/youtrack/issue/KOV-68) Add conversion from rx::Observable to kovenant::Promise
* [KOV-69](http://issues.komponents.nl/youtrack/issue/KOV-69) Add conversion from kovenant::Promise to rx::Observable

**core**

* [KOV-70](http://issues.komponents.nl/youtrack/issue/KOV-70) Leverage sun.misc.Unsafe, fallback to AtomicFieldUpdaters

##v3.0.0

**general**

* Kotlin 1.0

##v3.0.0-rc.1036.1

**core**

* [KOV-67](http://issues.komponents.nl/youtrack/issue/KOV-67) Duplicate execution of task on shutdown boundary

##v3.0.0-rc.1036

**core**

* [KOV-66](http://issues.komponents.nl/youtrack/issue/KOV-66) Make LazyPromise implement kotlin Lazy

##v3.0.0-beta.4

**core**

* [KOV-63](http://issues.komponents.nl/youtrack/issue/KOV-63) rename `async` to `task`


##v3.0.0-beta.3

**general**

* [KOV-27](http://issues.komponents.nl/youtrack/issue/KOV-27) Remove deprecated API

**android**

* [KOV-62](http://issues.komponents.nl/youtrack/issue/KOV-62) Background threads on android should have lower priority


##v2.9.0

**general**

* [KOV-59](http://issues.komponents.nl/youtrack/issue/KOV-59) Kotlin version 1.0.0-beta-1038

**core**

* [KOV-57](http://issues.komponents.nl/youtrack/issue/KOV-57) Allow for daemon threads


##v2.8.0

**jvm**

* [KOV-15](http://issues.komponents.nl/youtrack/issue/KOV-15) Throttle max current processes

##v2.7.0

**general**

* [KOV-56](http://issues.komponents.nl/youtrack/issue/KOV-56) Kotlin M14

##v2.6.0

**general**

* [KOV-53](http://issues.komponents.nl/youtrack/issue/KOV-53) Allow `null` values

**core**

* [KOV-49](http://issues.komponents.nl/youtrack/issue/KOV-49) Better `void`/`Unit` support
* [KOV-54](http://issues.komponents.nl/youtrack/issue/KOV-54) Covariant promise API
* [KOV-55](http://issues.komponents.nl/youtrack/issue/KOV-55) `then` optimizations

##v2.5.0

Updated Kovenant to Kotlin M13.

**general**

* [KOV-50](http://issues.komponents.nl/youtrack/issue/KOV-50) Kotlin M13

**core**

* [KOV-51](http://issues.komponents.nl/youtrack/issue/KOV-51) public interfaces
* [KOV-52](http://issues.komponents.nl/youtrack/issue/KOV-52) default getError bug


##v2.4.0

**general**

This release has generified UI callback support and introduced JavaFX support based on the new UI callback support.
For more information see the [Kovenant document site](http://kovenant.komponents.nl) about the 
[Ui Project](http://kovenant.komponents.nl/addons/ui/).

* [KOV-44](http://issues.komponents.nl/youtrack/issue/KOV-44) UI Callback project
* [KOV-35](http://issues.komponents.nl/youtrack/issue/KOV-35) JFX Dispatcher


##v2.3.3

**progress**

* [KOV-45](http://issues.komponents.nl/youtrack/issue/KOV-45) Updated progress

##v2.3.2

**general**

* Updated kotlin version to 0.12.1230
* Improved testing of get()

##v2.3.1

**core**

* [KOV-43](http://issues.komponents.nl/youtrack/issue/KOV-43) Fix for broken get()

##v2.3.0

**core**

* [KOV-36](http://issues.komponents.nl/youtrack/issue/KOV-36) Ability to control JVM thread factory

**kovenant-functional**

* [KOV-37](http://issues.komponents.nl/youtrack/issue/KOV-37) [Unwrap](http://kovenant.komponents.nl/api/functional_usage/#unwrap) method
* [KOV-38](http://issues.komponents.nl/youtrack/issue/KOV-38) Functional constructs. [map](http://kovenant.komponents.nl/api/functional_usage/#map)  / [bind](http://kovenant.komponents.nl/api/functional_usage/#bind)  / [apply](http://kovenant.komponents.nl/api/functional_usage/#apply)  / [withContext](http://kovenant.komponents.nl/api/functional_usage/#withcontext)  


##v2.2.1

**general**

* Updated kotlin version to 0.12.1218


##v2.2.0

**kovenant-android**

* [KOV-13](http://issues.komponents.nl/youtrack/issue/KOV-13) Improve performance successUI, failUI, alwaysUI callbacks

**kovenant-core**

* [KOV-12](http://issues.komponents.nl/youtrack/issue/KOV-12) Blocking get functionality
* [KOV-31](http://issues.komponents.nl/youtrack/issue/KOV-31) Allow wait strategies to be properly interrupted


##v2.1.1

**kovenant-disruptor**

* [KOV-33](http://issues.komponents.nl/youtrack/issue/KOV-33) Disruptor project lacks a description

**kovenant-core**

* [KOV-32](http://issues.komponents.nl/youtrack/issue/KOV-32) Empty collections with bulk operations are not properly handled

##v2.1.0

**kovenant-core**

* [KOV-28](http://issues.komponents.nl/youtrack/issue/KOV-28) Instant resolved Promises
* [KOV-26](http://issues.komponents.nl/youtrack/issue/KOV-26) Smoother configuration
* [KOV-25](http://issues.komponents.nl/youtrack/issue/KOV-25) Allow for custom context implementations
* [KOV-29](http://issues.komponents.nl/youtrack/issue/KOV-29) Better backwards compatibility


**kovenant-progress**

* [KOV-8](http://issues.komponents.nl/youtrack/issue/KOV-8) Progress tracking support

**kovenant-disruptor**

* [KOV-7](http://issues.komponents.nl/youtrack/issue/KOV-7) LMAX Disruptor queues

##v2.0.0
[KOV-21](http://issues.komponents.nl/youtrack/issue/KOV-21) Kovenant has a new home which is [komponents.nl](http://komponents.nl) and thus the project is now available as `nl.komponents.kovenant:kovenant:2.0.0`

**general:**

* [KOV-22](http://issues.komponents.nl/youtrack/issue/KOV-22) Kotlin M12

**kovenant-core:**

Mostly breaking API changes in this release:

* [KOV-18](http://issues.komponents.nl/youtrack/issue/KOV-18) Configurable Dispatcher per callback which allows for better integration with other platforms and libraries. _Thanks Jayson Minard_ 
* [KOV-20](http://issues.komponents.nl/youtrack/issue/KOV-20) Internally there is a new work queue for single consumer scenarios (default for callback dispatcher)
* [KOV-19](http://issues.komponents.nl/youtrack/issue/KOV-19) Fixed an bug where interrupted flags might not be properly cleared by the dispatcher
* [KOV-24](http://issues.komponents.nl/youtrack/issue/KOV-24) Changed the configuration structure (breaking)

##v1.1.0

**general:**

* restructured documentation, ordered by artifact

**kovenant-core:**

* added [Any](api/core_usage.md#any) functionality
* added [All](api/core_usage.md#all) functionality
* [KOV-11](http://issues.komponents.nl/youtrack/issue/KOV-11) added [cancellation](api/core_usage.md#cancel) ability to promises 
* [KOV-9](http://issues.komponents.nl/youtrack/issue/KOV-9) added [lazyPromise](api/core_usage.md#lazy-promise) property delegate

**kovenant-android:**

* [KOV-10](http://issues.komponents.nl/youtrack/issue/KOV-10) added easier [Android configuration](android/config.md)

##v1.0.0
The focus of this release has been on Android support.

* KOV-1 Android Dispatcher
* KOV-2 Android extension callback methods
* KOV-4 Improve project structure
* KOV-5 Introduce a blocking WaitStrategy

##v0.1.2

* Fixed a concurrency issue in the callback queue that could lead to NPE's 

##v0.1.1

* Made kotlin library an optional dependency to not interfere with peoples projects

##v0.1.0
Initial release.
Bringing Promises to Kotlin. Configuration free.