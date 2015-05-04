#Android
part of [`kovenant-android`](../index.md#artifacts)

---

While Kovenant is perfectly usable on Android as-is, there are a couple things that are specific to the platform.
One is that Android applications can only interact with the interface through the main thread. By default
Kovenant operates on its own instantiated threads.

There are two ways we can achieve interacting with the main thread, besides the standard Android facilities of course.
On is by specific extensions methods and the other is by a specific `Dispatcher`.

##Extension methods
The most flexible way of interacting with the main thread is by using the extension methods. The `kovenant-android` 
library provides `successUI`, `failUI` and `alwaysUI`. They operate just like their 
[regular counterparts](../api/callbacks.md) except their bodies are executed on the Android main thread. Both type of 
callbacks can be mixed freely.

```kt
async {
    foo() //produces 'bar'
} success {
    //no need to do this on the
    //main thread
    bar -> writeLog(bar)
} successUI {
    //also update the interface
    bar -> updateUI(bar)
}
```

##Dispatcher
_to implement_
You can configure Kovenant to dispatch all callbacks on the main thread by using the `AndroidUIDispatcher`. 

```kt
Kovenant.configure {
    callbackDispatcher = androidUIDispatcher()
}
```

