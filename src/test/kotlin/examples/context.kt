package examples.context

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.async
import nl.mplatvoet.komponents.kovenant.buildDispatcher

fun main(args: Array<String>) {
    val ctx = Kovenant.createContext {
        callbackDispatcher = buildDispatcher { name = "cb-new" }
        workerDispatcher = buildDispatcher { name = "work-new" }
    }

    async {
        println("default async $threadName")
    } success {
        println("default success $threadName")
    }

    async(ctx) {
        println("ctx async $threadName")
    } success {
        println("ctx success $threadName")
    }
}

private val threadName : String get() = Thread.currentThread().getName()
