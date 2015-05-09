#Android features
part of [`kovenant-android`](../index.md#artifacts)

---
While Kovenant is perfectly usable on Android as-is, there are a couple things that are specific to the platform.
One is that Android applications can only interact with the interface through the main thread. By default
Kovenant operates on its own maintained pool of threads and thus can't update the UI.

There are two ways we can achieve interacting with the main thread, besides the standard Android facilities of course.
One is by specific extensions methods and the other is by a specific `Dispatcher`.

##UI callbacks
The most flexible way of interacting with the main thread is by using the extension methods. The `kovenant-android` 
library provides `successUi`, `failUi` and `alwaysUi`. They operate just like their 
[regular counterparts](../api/core_usage.md#callbacks) except their bodies are executed on the Android main thread. Both type of 
callbacks can be mixed freely.

```kt
async {
    foo() //produces 'bar'
} success {
    //no need to do this on the
    //main thread
    bar -> writeLog(bar)
} successUi {
    //also update the interface
    bar -> updateUI(bar)
}
```
The huge advantage of this approach is that operations on the main thread are explicitly chosen. So when in doubt,
this should be the preferred way of interacting with the main thread. 

##Start on UI thread
Just like Androids `AsyncTask` you might want to do some preparations on the UI thread before you start your background
work. This is what `promiseOnUi` does, it schedules a task on the UI thread and returns a `Promise` on which you can 
chain along.
 
```kt
promiseOnUi {
    //prepare the UI
} then {
    //do background work
} successUi {
    //post the results
}
```

##Dispatcher
You can configure Kovenant to dispatch all callbacks on the main thread by using the `androidUiDispatcher()`. 

>Please note, this approach can have a **serious negative effect on Androids UI performance** since you can delegate
>to much work to the UI thread way too easy.

That all being said. You, of course, know what you are doing.  

```kt
Kovenant.configure {
    callbackDispatcher = androidUiDispatcher()
}
```

###Loopers
You can also create a Dispatcher based on a [Looper](http://developer.android.com/reference/android/os/Looper.html)
instance. As a prerequisite the `Looper` has to be prepared already. 
See [`Looper.prepare()`](http://developer.android.com/reference/android/os/Looper.html#prepare()) 

```kt
buildLooperDispatcher(looper: Looper, type: DispatcherType)
```
The `DispatcherType` is either `BASIC` (default) or `FULL`. The main difference between the two types is that of
the `FULL` has all the methods, like `stop` and `cancel` implemented where `BASIC` has not. The reason for this
distinction is that keeping track of what is running and can be cancelled just uses a lot more resources. This might
not be an issue for background threads but can most certainly be an issue for the main/UI thread.

If you want to [convert](../api/jvm_usage.md) back and forth between `Executor`s and `Dispatcher`s you probably 
want to use a `FULL` `DispatcherType`, otherwise you are better of with a `BASIC` one.

##Demo app
For a workable demo please checkout the [Demo App on Github](https://github.com/mplatvoet/kovenant-android-demo).