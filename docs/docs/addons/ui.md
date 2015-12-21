#UI features
part of [`kovenant-ui`](../index.md#artifacts)

---
Frameworks involving UIs are commonly implemented in a way that only one thread is allowed to interact with that UI. 
Android, Swing, AWT and JavaFX all use this strategy. For this reason Kovenant-UI offers a generic way for executing
callbacks of promises on that specific UI thread. 


##UI callbacks
The most flexible way of interacting with the main thread is by using the extension methods. The `kovenant-ui` 
library provides `successUi`, `failUi` and `alwaysUi`. They operate just like their 
[regular counterparts](../api/core_usage.md#callbacks) except their bodies are executed on the configured UI thread. Both type of 
callbacks can be mixed freely. 

```kt
val promise = task {
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
The huge advantage of this approach is that operations on the main thread are explicitly chosen. 

##Start on UI thread
You might want to do some preparations on the UI thread before you start your background
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

##Configuring
Kovenant UI just requires a minimal setup: It needs to know where to dispatch the callback to. So all that is needed
is configure it with a `Dispatcher` that operates on the desired thread, like this:

```kt
KovenantUi.uiContext {
    dispatcher = myUiDispatcher
}
```

###ProcessAwareDispatcher
When you configure Kovenant UI with a `ProcessAwareDispatcher` calls can sometimes be optimized. A `ProcessAwareDispatcher`
knows which threads/tasks/processes it owns and therefor scheduling of tasks can sometimes be avoided. All the ui callbacks
have a parameter `alwaysSchedule`, which is `false` by default, which tells whether a specific callback always gets
scheduled/queued or that it may be optimized.