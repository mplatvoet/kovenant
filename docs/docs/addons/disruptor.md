#LMAX Disruptor
part of [`kovenant-disruptor`](../index.md#artifacts)

---
The inter-thread messaging library [LMAX Disruptor](https://lmax-exchange.github.io/disruptor/) is well known for 
it's performance. Kovenant can be configured to tap into this performance and use LMAX Disruptor as the `WorkQueue`.

##Configuring
You simply configure Kovenant to use the Disruptor as follows:
```kt
Kovenant.context {
    callbackContext {
        dispatcher {
            concurrentTasks = 1
            workQueue = disruptorWorkQueue()
        }
    }
}
```

The `WorkQueue` created by `disruptorWorkQueue()` is Multiple Producer and Multiple Consumer safe. By default the 
created queue has a capacity of 1024 tasks. You can also specify a custom capacity by calling 
`disruptorWorkQueue(capacity = 2048)`. Note that the Disruptor needs to have a capacity that is a power of two. If you
specify a capacity that is not a power of two Kovenant will round up to the nearest power of two capacity. 

##Blocking 
If the maximum capacity of the Disruptor `WorkQueue` is reached the producers will block. So this is different to
the default behaviour of Kovenant which is unbounded and non-blocking. The consumers follow the rules depending
on the configured [poll strategies](../api/core_config.md#builddispatcher)