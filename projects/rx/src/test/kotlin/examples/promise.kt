package nl.komponents.kovenant.rx.examples

import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.rx.EmitStrategy
import nl.komponents.kovenant.rx.toListPromise
import nl.komponents.kovenant.rx.toPromise
import nl.komponents.kovenant.testMode
import rx.Observable
import rx.schedulers.Schedulers


fun main(args: Array<String>) {
    Kovenant.testMode { }

    //Default behaviour, take first element, exception on empty Observable
    Observable.from(arrayOf(1, 2, 3, 4, 5)).toPromise().printResult()
    Observable.from(arrayOf<Int>()).toPromise().printResult()

    //Take last element
    Observable.from(arrayOf(1, 2, 3, 4, 5)).toPromise(strategy = EmitStrategy.LAST).printResult()

    //default value on empty Observable
    Observable.from(arrayOf<Int>()).toPromise() { 42 }.printResult()


    //Put everything in a list
    Observable.from(arrayOf(1, 2, 3, 4, 5)).toListPromise().printResult()
    Observable.from(arrayOf<Int>()).toListPromise().printResult()
}

fun <V, E> Promise<V, E>.printResult() = success {
    println("Success value = $it")
} fail {
    println("Fail value = $it")
}