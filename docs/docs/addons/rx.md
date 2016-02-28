#Rx features
part of [`kovenant-rx`](../index.md#artifacts)

---
Addon project for converting back and forth between `Kovenant` `Promise`s and rx `Observable`s.  


##ToPromise
Any `Observable` can be turned into a `Promise` but understand that there is a fundamental difference between the two.
A `Promise` represents a single value that either is successful or failed. An `Observable` on the other hand represents 
a stream of values which results in the following possible states:

* emit nothing.
* emit an error.
* emit completed without data
* emit completed after single value
* emit completed after multiple values
* emit multiple values but never completed

To convert from an `Observable` to a `Promise` you simply do:

```kt
val values = arrayOf(1, 2, 3, 4, 5)
val observable = Observable.from(values)
val promise = observable.toPromise()
```

By default `toPromise()` will create a promise that will resolve successful with the first emitted value. It will
resolve as failed if there are no values emitted but the `Observable` emits completed. This will thus lead to:

* emit nothing - _promise doesn't complete_
* emit an error - _promise resolves as failed, if no other value was emitted_
* emit completed without data - _promise resolves as failed_
* emit completed after single value - _promise resolves successful with value_
* emit completed after multiple values - _promise resolves successful with first value_
* emit multiple values but never completed - _promise resolves successful with first value_

###EmitStrategy
By default `toPromise()` resolves successful with the first emitted value. If you'd rather resolve by the last emitted
value you can change the behaviour by calling:

```kt
val promise = observable.toPromise(strategy = EmitStrategy.LAST)
```

###EmptyPolicy
By default `toPromise()` resolves as failed if the `Observable` is completed but hasn't emitted a value. To control
the behaviour of the promise when the `Observable` is empty you can create one of the following EmptyPolicies:

```kt
//resolve with value
EmptyPolicy.resolve (42)

//resolve with factory value
EmptyPolicy.resolve { 42 }

//reject with eception
EmptyPolicy.reject(Exception())

//reject with exception factory
EmptyPolicy.reject { Exception() }

```

And you can use them like this:

```kt
val promise = observable.toPromise(emptyPolicy = EmptyPolicy.resolve (42))
```

There is also a shorthand for this common case, so you can always resolve successful with a default value:

```kt
val promise = observable.toPromise(42)
//or with a factory
val promise = observable.toPromise() { 42 }
```

##ToListPromise

TODO

##ToObservable

TODO