#Changelog

Changelog of Kovenant. Complying to [Semantic Versioning](http://semver.org).

---

##Upcomming

###v1.2.0
* [KOV-8](http://komponents.myjetbrains.com/youtrack/issue/KOV-8) Progress tracking support

---

##Released

###v1.1.0

general:
* restructured documentation, ordered by artifact

kovenant-core:
* added [Any](api/core_usage.md#any) functionality
* added [All](api/core_usage.md#all) functionality
* [KOV-11](http://komponents.myjetbrains.com/youtrack/issue/KOV-11) added [cancellation](api/core_usage.md#cancel) ability to promises 
* [KOV-9](http://komponents.myjetbrains.com/youtrack/issue/KOV-9) added [lazyPromise](api/core_usage.md#lazy-promise) property delegate

kovenant-android:
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