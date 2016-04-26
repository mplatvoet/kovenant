#Android configuration
part of [`kovenant-android`](../index.md#artifacts)

---

Kovenant works just fine without any configuration but for Android the default settings are probably
not best suited. Because by default, if there isn't any work left, Kovenant shuts down it's worker and callback threads.
Normally this is just fine but on Android it's quite common to do some work on user input. That would lead to
a constant creation and destruction of threads. That's not good for responsiveness nor for battery life.
 
So we need to keep our threads alive. That also implies we need to tell when to shut them down again. Kovenant
introduces two convenience functions, `startKovenant()` and `stopKovenant()`, to keep threads alive and shut them 
down at the proper time again. This only needs to be done once per application. 

So the recommended way to setup (and shutdown) Kovenant is by providing your own [`Application`](http://developer.android.com/reference/android/app/Application.html) implementation:

```kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Configure Kovenant with standard dispatchers
        // suitable for an Android environment.
        startKovenant()
    }

    override fun onTerminate() {
        super.onTerminate()
        // Dispose of the Kovenant thread pools.
        // For quicker shutdown you could use
        // `force=true`, which ignores all current
        // scheduled tasks
        stopKovenant()
    }
}
```

Don't forget to properly setup your application in your `AndroidManifest.xml`:

```xml
<application
        android:name=".MyApplication">
        
        <!--activities etc.-->
</application>
```

Note that `stopKovenant(force: Boolean = false)` also has a `force` parameter that defaults to `false`. So by default 
all the queues are depleted. If you don't want this you can always use `force = true` to immediately shutdown the 
Dispatchers. `stopKovenant` never blocks though.

And just as a reminder, these are just convenience functions. You can always [configure Kovenant manually](../api/core_config.md).