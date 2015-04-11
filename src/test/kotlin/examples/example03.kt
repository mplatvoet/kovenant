package examples.example03

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.buildDispatcher


fun main (args: Array<String>) {
    Kovenant.configure {
        workerDispatcher = buildDispatcher {
            name = "Bob the builder"
            numberOfThreads = 2
            configureWaitStrategy {
                addBusyPoll(1000)
                addSleepPoll(10, 10)
            }
        }
        callbackDispatcher = buildDispatcher {
            name = "Tank"
            numberOfThreads = 1
        }
    }
}

