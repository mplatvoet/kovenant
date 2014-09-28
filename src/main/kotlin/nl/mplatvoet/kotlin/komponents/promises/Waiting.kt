package nl.mplatvoet.kotlin.komponents.promises

import java.util.concurrent.TimeUnit
import java.util.concurrent.CountDownLatch

/**
 * Created by mark on 27/09/14.
 */
public fun Promises.await(vararg promises: Promise<*>): Unit = latchFor(*promises).await()
//public fun Promises.await(vararg promises: Promise<*>): Unit = latchFor(promises).await()
public fun Promises.await(timeout: Long, unit: TimeUnit, vararg promises: Promise<*>): Boolean = latchFor(*promises).await(timeout, unit)

private fun latchFor(vararg promises: Promise<*>): CountDownLatch {
    val latch = CountDownLatch(promises.size)
    promises.forEach {
        it.always { latch.countDown() }
    }
    return latch
}