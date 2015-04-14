package examples.configuration

import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.buildDispatcher


fun main (args: Array<String>) {
    Kovenant.configure {
        //Specify a new worker dispatcher.
        //this dispatcher is responsible for work that is executed by async and then functions
        //so this is basically work that is expected to run a bit longer
        workerDispatcher = buildDispatcher {
            name = "Bob the builder"
            numberOfThreads = 2
            configureWaitStrategy {
                addBusyPoll(1000)
                addSleepPoll(10, 10)
            }
        }

        //Specify a new callback dispatcher.
        //this dispatcher is responsible for callbacks like success, fail and always.
        //it is expected that these callback do very little work and never block
        callbackDispatcher = buildDispatcher {
            name = "Tank"
            numberOfThreads = 1
        }

        // route internal errors when invoking callbacks.
        // this is also the place to route this to a preferred logging framework
        callbackError = fun (e:Exception) : Unit = e.printStackTrace(System.err)

        // when promises are being resolved multiple time, which is misuse of the api
        // this method is fired. You can for instance choose to throw an Exception here
        multipleCompletion = fun (a:Any, b:Any) : Unit = System.err.println("Tried resolving with $b, but is $a")
    }
}

