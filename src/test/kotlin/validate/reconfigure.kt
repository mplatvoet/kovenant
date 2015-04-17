package validate.reconfigure

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.all
import nl.mplatvoet.komponents.kovenant.async
import nl.mplatvoet.komponents.kovenant.buildDispatcher
import support.fib
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    validate(10000)
}


fun validate(n:Int) {
    val errors = AtomicInteger()
    val successes = AtomicInteger()

    val firstBatch = Array(n) { n ->
        errors.incrementAndGet()
        async {
            val i = Random().nextInt(10)
            Pair(i, fib(i))
        } success {
            errors.decrementAndGet()
            successes.incrementAndGet()
        }
    }

    Kovenant.configure {
        workerDispatcher = buildDispatcher { numberOfThreads = 2; }
        callbackDispatcher = buildDispatcher { numberOfThreads = 1 }
    }

    val secondBatch = Array(n) { n ->
        errors.incrementAndGet()
        async {
            val i = Random().nextInt(10)
            Pair(i, fib(i))
        } success {
            errors.decrementAndGet()
            successes.incrementAndGet()
        }
    }


    val promises = array(*firstBatch, *secondBatch)

    all(*promises) always {
        println("validate with ${n*2} attempts, errors: ${errors.get()}, successes: ${successes.get()}")
    }
}
