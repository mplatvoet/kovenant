package examples.example03

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.buildDispatcher


fun main (args: Array<String>) {
    Kovenant.configure {
        workerDispatcher = buildDispatcher {
            name = "Bob the builder"
            numberOfThreads = 2
            configureWaitStrategy {
                
            }
        }
        callbackDispatcher = buildDispatcher {
            name = "Tank"
            numberOfThreads = 1
        }
    }
}

