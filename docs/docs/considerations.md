#Considerations
Some thoughts and considerations that lead to design decisions.

##Promise specs
There are quite a few [implementations](http://en.wikipedia.org/wiki/Futures_and_promises#List_of_implementations) out there. 
All with their strength and weaknesses and adapted to the language their written in. It's tempting to follow an 
existing spec like [Promises/A+](https://promisesaplus.com/). I have chosen not to follow this because I think it doesn't play
well with Kotlin. 

For instance, the [Promises/A+](https://promisesaplus.com/) spec defines `then` as
```js
promise.then(onFulfilled, onRejected)
```
where `onFulfilled` and `onRejected` are two function arguments. Both are basically `bind` methods on _either_ the 
 _left_ or _right_ side of a promise. This introduces some challenges that are hard to overcome. The biggest one is how
 to handle errors thrown from the `onFulfilled` or `onRejected` method. The [Promises/A+](https://promisesaplus.com/)
 states the following: 

>If either `onFulfilled` or `onRejected` throws an exception `e`, `promise2` must be rejected with `e` as the reason.

This means 1 promise can actually resolve in 3 states instead of the probably expected 2, which are:

* _fulfilled_ with value
* _rejected_ with reason
* _rejected_ by error from either `onFulfilled` or `onRejected`

I don't think this makes much sense and is not what expected when using the API. Therefor the signature for Kovenant's
`then` function is:
```kt
fun <V, R> Promise<V, Exception>.then(bind: (V) -> R): Promise<R, Exception>
```
Any `Exception` from the bind method is considered a rejection and a successful computation a fulfillment. That way 
the Promise resolves in just 2 states. Kovenant's `then` behaves quite similar to Option/Maybe/Try types for that matter.
 
So reason enough to not follow this spec. 

##Non Blocking
Most Promises implementations provide functions like `isDone() : Boolean` and blocking functions `get() : V` as an alternative for 
working with callbacks. Though it's fairly easy to implement I haven't done so yet. I feel it's defying the purpose 
of having a promises API. 

Though this might just one of this topics where I will change my mind. Kovenant should make it easier to work with 
async jobs, not harder. But one thing is for sure though, if I add it to the library it will be an extension function.
If you need now, it looks something like this:
```kt
fun <V:Any> Promise<V, Exception>.get() : V {
    val latch = CountDownLatch(1)
    val e = AtomicReference<Exception>()
    val v = AtomicReference<V>()

    this.success {
        v.set(it)
        latch.countDown()
    } fail {
        e.set(it)
        latch.countDown()
    }
    latch.await()
    val exception = e.get()
    if (exception !=null) throw exception
    return v.get()
}
```

