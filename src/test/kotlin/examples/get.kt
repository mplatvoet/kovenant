package examples.get


import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.Promise
import nl.mplatvoet.komponents.kovenant.all
import nl.mplatvoet.komponents.kovenant.async
import support.fib
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference


fun main(args: Array<String>) {
    val (n, fib) = async { Pair(30, fib(30)) }.get()
    println("fib($n) = $fib")
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

