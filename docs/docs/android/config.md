#Android configuration
part of [`kovenant-android`](../index.md#artifacts)

---

Kovenant works just fine without any configuration but for Android the default settings are probably
not best suited. Because by default, if there isn't any work left, Kovenant shuts down it's worker and callback threads.
Normally this is just fine but on Android it's quite common to do some work on user input. That would lead to
a constant creation and destruction of threads. That's not good for responsiveness nor for battery life.
 
So we need to keep our threads alive. That also implies we need to tell when to shut them down again. Kovenant
introduces two convenience functions, `startKovenant()` and `stopKovenant()`, to keep threads alive and shut them 
down at the proper time again.

So it all comes down to this:

```kt
public class MainActivity : ... {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(...)

        // Configure Kovenant with standard dispatchers
        startKovenant()

    }

    ...

    override fun onDestroy() {
        // Dispose of the Kovenant thread pools
        // for quicker shutdown you could use
        // force=true, which ignores all current
        // scheduled tasks
        stopKovenant()
        super.onDestroy()
    }
}
```

Note that `stopKovenant(force: Boolean = false)` also has a `force` parameter that defaults to `false`. So by default 
all the queues are depleted. If you don't want this you can always use `force = true` to immediately shutdown the 
Dispatchers. `stopKovenant` never blocks though.

And just as a reminder, these are just convenience functions. You can always [configure Kovenant manually](../api/core_config.md).