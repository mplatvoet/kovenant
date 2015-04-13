package examples.example04


import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.Promise
import nl.mplatvoet.komponents.kovenant.all
import nl.mplatvoet.komponents.kovenant.async
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


fun main(args: Array<String>) {

    val promise = Kovenant.async {
        Pair(13, fib(13))
    }


    Kovenant.async {
        Pair(13, fib(13))
    } success {
        pair -> println("fib(${pair.first}) = ${pair.second}")
    }

    println(promise.get())



}


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

//a very naive fibonacci implementation
fun fib(n: Int): Int {
    if (n < 0) throw IllegalArgumentException("negative numbers not allowed")
    return when (n) {
        0, 1 -> 1
        else -> fib(n - 1) + fib(n - 2)
    }
}
