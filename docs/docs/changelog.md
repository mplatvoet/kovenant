#Changelog

Changelog of Kovenant. Complying to [Semantic Versioning](http://semver.org).

---

##Upcomming

###v2.1.0
**certain**
* [KOV-26](http://komponents.myjetbrains.com/youtrack/issue/KOV-26) Smoother configuration

**probably**
* [KOV-8](http://komponents.myjetbrains.com/youtrack/issue/KOV-8) Progress tracking support

---

##Released

###v2.0.0
[KOV-21](http://komponents.myjetbrains.com/youtrack/issue/KOV-21) Kovenant has a new home which is [komponents.nl](http://komponents.nl) and thus the project is now available as `nl.komponents.kovenant:kovenant:2.0.0`

**general:**
* [KOV-22](http://komponents.myjetbrains.com/youtrack/issue/KOV-22) Kotlin M12

**kovenant-core:**

Mostly breaking API changes in this release:
* [KOV-18](http://komponents.myjetbrains.com/youtrack/issue/KOV-18) Configurable Dispatcher per callback which allows for better integration with other platforms and libraries. _Thanks Jayson Minard_ 
* [KOV-20](http://komponents.myjetbrains.com/youtrack/issue/KOV-20) Internally there is a new work queue for single consumer scenarios (default for callback dispatcher)
* [KOV-19](http://komponents.myjetbrains.com/youtrack/issue/KOV-19) Fixed an bug where interrupted flags might not be properly cleared by the dispatcher
* [KOV-24](http://komponents.myjetbrains.com/youtrack/issue/KOV-24) Changed the configuration structure (breaking)

###v1.1.0

**general:**

* restructured documentation, ordered by artifact

**kovenant-core:**

* added [Any](api/core_usage.md#any) functionality
* added [All](api/core_usage.md#all) functionality
* [KOV-11](http://komponents.myjetbrains.com/youtrack/issue/KOV-11) added [cancellation](api/core_usage.md#cancel) ability to promises 
* [KOV-9](http://komponents.myjetbrains.com/youtrack/issue/KOV-9) added [lazyPromise](api/core_usage.md#lazy-promise) property delegate

**kovenant-android:**

* [KOV-10](http://komponents.myjetbrains.com/youtrack/issue/KOV-10) added easier [Android configuration](android/config.md)

###v1.0.0
The focus of this release has been on Android support.

* KOV-1 Android Dispatcher
* KOV-2 Android extension callback methods
* KOV-4 Improve project structure
* KOV-5 Introduce a blocking WaitStrategy

###v0.1.2

* Fixed a concurrency issue in the callback queue that could lead to NPE's 

###v0.1.1

* Made kotlin library an optional dependency to not interfere with peoples projects

###v0.1.0
Initial release.
Bringing Promises to Kotlin. Configuration free.