#Configuration
Configuration of Kovenant can easily be done in code. Configuring Kovenant is completely threadsafe, so Kovenant
can be reconfigured during a running application from multiple threads. But you probably want to do this when 
your application starts so that you don't create much waste.

##Full example
```kt
Kovenant.configure {
    //Specify a new worker dispatcher.
    //this dispatcher is responsible for work that is executed by async and then functions
    //so this is basically work that is expected to run a bit longer
    workerDispatcher = buildDispatcher {
        //Name this dispatcher, threads created by this dispatcher will get this name with a number appended 
        name = "Bob the builder"
        
        //the max number of threads this dispatcher keeps running in parallel. During the lifetime of this
        //dispatcher the number of threads created can be far greater because threads also get destroyed.
        numberOfThreads = 2
        
        //Configure the strategy to apply to a thread when there is no work left in the queue. Note that
        //when the strategy finishes the thread will shutdown. Strategies are applied in order of configuration and
        //resets after a thread executes any new task.
        configureWaitStrategy {
            //A busy poll strategy simple polls the provided amount of polls without interrupting the thread.                
            addBusyPoll(numberOfPolls = 1000)
            
            //A sleep poll strategy simply puts the thread to sleep between polls.
            addSleepPoll(numberOfPolls = 100, sleepTimeInMs = 10)
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
```