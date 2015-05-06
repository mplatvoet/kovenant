#Android configuration
part of [`kovenant-android`](../index.md#artifacts)

---

Kovenant works just fine without any configuration nut for Android the default settings are probably
not best suited. By default, if there isn't any work left, Kovenant shuts down it's worker and callback threads.
Normally this is just fine but on Android it's quite common to do some work on user input. That would lead to
a constant creation and destruction of threads. That's not good for responsiveness nor for battery life.
 
So we need to keep our threads alive. That also implies we need to tell when to shut them down again. Kovenant
introduces a convenience function `configureKovenant()` to keep threads alive and provides a handle to shut them 
down again.

So it all comes down to this:

```kt
public class MainActivity : ... {
    var disposable : Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(...)

        // Configure Kovenant with standard dispatchers
        // obtain a reference to a Disposable to shutdown
        // the thread pools on exit.
        disposable = configureKovenant()

    }

    ...

    override fun onDestroy() {
        // Dispose of the Kovenant thread pools
        // for quicker shutdown you could use
        // force=true, which ignores all current
        // scheduled tasks
        disposable?.close()
        super.onDestroy()
    }
}
```

`configureKovenant()`

