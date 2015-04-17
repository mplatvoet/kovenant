package examples.all

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.all
import nl.mplatvoet.komponents.kovenant.async
import support.fib


fun main(args: Array<String>) {
    val promises = Array(10) { n ->
        async {
            Pair(n, fib(n))
        }
    }

    all(*promises) success {
        it forEach {pair -> println("fib(${pair.first}) = ${pair.second}")}
    } always {
        println("All ${promises.size()} promises are done.")
    }

}
