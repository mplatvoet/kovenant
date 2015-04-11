package examples.example01

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.all
import nl.mplatvoet.komponents.kovenant.async


fun main(args: Array<String>) {
    val promises = Array(10) { n ->
        Kovenant.async {
            Pair(n, fib(n))
        } success {
            pair -> println("fib(${pair.first}) = ${pair.second}")
        }
    }

    Kovenant.all(*promises) always {
        println("All ${promises.size()} promises are done.")
    }

}


//a very naive fibonacci implementation
private fun fib(n: Int): Int {
    if (n < 0) throw IllegalArgumentException("negative numbers not allowed")
    return when (n) {
        0, 1 -> 1
        else -> fib(n - 1) + fib(n - 2)
    }
}