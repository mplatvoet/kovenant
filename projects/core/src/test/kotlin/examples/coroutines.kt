package examples.coroutines

import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.async

fun main(args: Array<String>) {
    val promise = async<Int> {
        await(Promise.of(13)) + 15
    }

    promise success {
        println(it)
    }
}


