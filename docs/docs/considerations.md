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

>>If either `onFulfilled` or `onRejected` throws an exception `e`, `promise2` must be rejected with `e` as the reason.

This means 1 promise can actually resolve in 3 states instead of the probably expected 2, which are:
* _fulfilled_ with new value
* _rejected_ with new reason
* _rejected_ by error from either `onFulfilled` or `onRejected`

  