#UI features
part of [`kovenant-ui`](../index.md#artifacts)

---
Frameworks involving UIs are commonly implemented in a way that only one thread is allowed to interact with that UI. 
Android, Swing, AWT and JavaFX all use this strategy. For this reason Kovenant-UI offers a generic way for executing
callbacks of promises on that specific UI thread. 

There are two ways we can achieve interacting with the UI thread.
One is by specific extensions methods and the other is by a specific `Dispatcher`.

##UI callbacks
The most flexible way of interacting with the main thread is by using the extension methods. The `kovenant-ui` 
library provides `successUi`, `failUi` and `alwaysUi`. They operate just like their 
[regular counterparts](../api/core_usage.md#callbacks) except their bodies are executed on the configured UI thread. Both type of 
callbacks can be mixed freely. If a callback is added to an already resolved `Promise` this gets executed immediately
without scheduling. If you want to force scheduling just pas `alwaysSchedule = true` along.

```kt
val promise = async {
    foo() //produces 'bar'
} 

promise success {
    //no need to do this on the
    //main thread
    bar -> writeLog(bar)
} 

promise successUi {
    //also update the interface
    bar -> updateUI(bar)
}
```
The huge advantage of this approach is that operations on the main thread are explicitly chosen. So when in doubt,
this should be the preferred way of interacting with the main thread. 

##Start on UI thread
Just like Androids `AsyncTask` you might want to do some preparations on the UI thread before you start your background
work. This is what `promiseOnUi` does, it schedules a task on the UI thread and returns a `Promise` on which you can 
chain along. If this is called from the UI Thread this gets executed immediately without scheduling. If you want to 
force scheduling just pas `alwaysSchedule = true` along.
 
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
You can, of course, configure Kovenant to dispatch all callbacks on the UI thread by default. Just  

>Please note, this approach can have a **serious negative effect on the platforms UI performance** since you can delegate
>to much work to the UI thread way too easy.

That all being said. You, of course, know what you are doing.  

```kt
Kovenant.context {
    callbackContext.dispatcher = myUiDispatcher
}
```