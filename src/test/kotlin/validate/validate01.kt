package validate.validate01

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.all
import nl.mplatvoet.komponents.kovenant.async
import support.fib
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    validate(1000000)
}


fun validate(n:Int) {
    val errors = AtomicInteger()
    val successes = AtomicInteger()
    val promises = Array(n) { n ->
        errors.incrementAndGet()
        async {
            val i = Random().nextInt(10)
            Pair(i, fib(i))
        } success {
            errors.decrementAndGet()
            successes.incrementAndGet()
        }
    }

    all(*promises) always {
        println("validate with $n attempts, errors: ${errors.get()}, successes: ${successes.get()}")
    }
}