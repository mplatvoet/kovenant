package examples.await

import nl.mplatvoet.komponents.kovenant.*
import java.util.concurrent.CountDownLatch



public fun await(vararg promises: Promise<*, *>) {
    val latch = CountDownLatch(promises.size())
    promises forEach {
        p ->
        p always { latch.countDown() }
    }
    latch.await()
}



fun main (args: Array<String>) {
    val promises = Array(50) {Kovenant.async { Thread.sleep(100L) }}

    print("waiting...")
    await(*promises)
    println(" done.")
}