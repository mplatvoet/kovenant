package nl.mplatvoet.kotlin.komponents.promises

/**
 * Created by mplatvoet on 30-5-2014.
 */

fun main(args: Array<String>) {
    val promises = Array(10) {


        Promises.async {
            it
        }.then {
            val sleep = (Math.random() * 1000).toLong()
            Thread.sleep(sleep)
            println("Thread[${Thread.currentThread().getName()}] $it, sleep[$sleep ms]")
            it
        }.success {
            println("Thread[${Thread.currentThread().getName()}] $it, done.")
        }


    }

    Promises.await(*promises)
}

