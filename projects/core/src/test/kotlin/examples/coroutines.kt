package examples.coroutines

import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.async
import nl.komponents.kovenant.await

fun main(args: Array<String>) {
    val promise = async {
        await(Promise.of(13)) + 15
    }

    promise success ::println
}


