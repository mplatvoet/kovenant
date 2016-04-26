package examples

import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.unwrap

fun main(args: Array<String>) {
    val l = task {
        Promise.of(1)
    }.unwrap().then {
        Promise.of(it + 1)
    }.unwrap()

    l success {
        println(it)
    }
}

