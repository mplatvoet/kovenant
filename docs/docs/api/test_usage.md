#Kovenant Test Usage
part of [`kovenant-core`](../index.md#artifacts)

---

##TestMode
Kovenant by default, without any configuration, will create (and destroy) threads in order to do it's work.
Although this is desirable for normal operations, it is not for testing. Tests should be deterministic and therefor
Kovenant ships with a `testMode()`. In this mode Kovenant is configured with sequential `Dispatcher`s and all errors
are routed to the provided failure callback handler. Make sure to let your tests fail on any unexpected exceptions.

So it's as simple as:

```
Kovenant.testMode {
    fail(it.message)
}
```