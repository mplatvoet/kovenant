#Android
part of [`kovenant-android`](../index.md#artifacts)

---

While Kovenant is perfectly usable on Android as-is, there are a couple things that are specific to the platform.
One is that Android applications can only interact with the interface through the main thread. By default
Kovenant operates on its own maintained pool of threads.

There are two ways we can achieve interacting with the main thread, besides the standard Android facilities of course.
One is by specific extensions methods and the other is by a specific `Dispatcher`.

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
The huge advantage of this approach is that operations on the main thread are explicitly chosen. So when in doubt,
this should be the preferred way of interacting with the main thread. 

##Dispatcher
You can configure Kovenant to dispatch all callbacks on the main thread by using the `androidUIDispatcher()`. 

>Please note, this approach can have a *serious negative* effect *on* Androids UI *performance* since you can delegate
>to much work to the UI thread way too easy.

That all being said. You, of course, know what you are doing.  

```kt
Kovenant.configure {
    callbackDispatcher = androidUIDispatcher()
}
```

* _TODO_ - document full and basic dispatcher
* _TODO_ - document looper dispatcher

