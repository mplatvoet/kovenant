#Considerations
Some thoughts and considerations that led to design decisions.

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
of having a promises API. Though this might just one of this topics where I will change my mind, because Kovenant should 
make it easier to work with async jobs, not harder. 

If I decide to add additional functions for determining state and retrieval of values it will *not* be added to the
[Promise interface](https://github.com/mplatvoet/kovenant/blob/master/src/main/kotlin/promises-api.kt). Instead, 
I will implement it as [Extensions](http://kotlinlang.org/docs/reference/extensions.html) to the current interface. And
maybe, as an optimization, I will introduce a second/extended interface `PromisePlus` which will define this methods to 
which the extension function can delegate if the current promise is of the second/extended type. This will keep the 
`Promise` interface lean. 

###I need it now!
Still, if you desperately need such functionality already it's easy to implement on the Jvm as shown by 
[example04.kt](https://github.com/mplatvoet/kovenant/blob/master/src/test/kotlin/examples/example04.kt). It basically
 comes down to this:

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

##Top level functions
One important consideration is the use of top level functions versus `class` or 
[`object`](http://kotlinlang.org/docs/reference/object-declarations.html#object-declarations) bound functions. To
illustrate:
```kt
async {
    foo()
}
//versus
Kovenant.async {
    foo()
}
```
the latter has the advantage that it doesn't interfere with other frameworks that introduces such methods. But on the other
hand how many frameworks bringing async functionality does a project really need? So I decided to stick with top level
functions, which need to be imported explicitly anyway, to keep things simple.

##sun.misc.Unsafe
I started off with an implementation that was partly written in Java and used the `sun.misc.Unsafe` class. The upside
was that it was quick and very memory efficient. The downside that it requires proprietary API. So I decided that
using a generic code base outweighs the performance and memory efficiency gains. As a bonus everything is written in 
pure Kotlin now.