package nl.mplatvoet.kotlin.komponents.promises

import java.util.concurrent.Executor
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by mark on 29/11/14.
 */


trait Dispatcher {
    class object {
        fun fromExecutor(executor: Executor): Dispatcher = ExecutorDispatcher(executor)
    }
    /*
        submit a task to be executed
     */
    fun submit(task: () -> Unit)

    /*
        offer to help out this dispatcher by doing some work
        return true if more help is wanted, false otherwise
     */
    fun offerHelp(): Boolean
}


private class ExecutorDispatcher(private val executor: Executor) : Dispatcher {
    override fun submit(task: () -> Unit) {
        executor.execute(task)
    }

    override fun offerHelp(): Boolean = false

}

private class FixedPoolDispatcher(val numberOfThreads: Int = Runtime.getRuntime().availableProcessors()) : Dispatcher {
    {
        if (numberOfThreads < 1) throw IllegalArgumentException("numberOfThreads must be atleast 1 but was $numberOfThreads")
    }

    private val queue = ConcurrentLinkedQueue<() -> Unit>()


    override fun submit(task: () -> Unit) {
        throw UnsupportedOperationException()
    }

    override fun offerHelp(): Boolean {
        throw UnsupportedOperationException()
    }

}

[suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")]
private class ThreadContext(monitor: Any,
                            private val threadName: String,
                            private val sharedQueue: ConcurrentLinkedQueue<() -> Unit>) {
    private val _monitor = monitor as Object

    private val thread = Thread() { run() };

    {
        thread.setName(threadName)
        thread.setDaemon(true)
        thread.start()
    }


    private fun run() {
        while (!Thread.currentThread().isInterrupted()) {
            val fn = sharedQueue.poll()
            if (fn != null) {
                fn()
            } else {
                synchronized(_monitor) {
                    //need some nap time
                    try {
                        while (sharedQueue.isEmpty()) _monitor.wait()
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt() // set the interrupted flag again
                    }
                }
            }
        }
    }

    fun interrupt() = thread.interrupt()
}


//
//private val executorDelegate: ThreadSafeLazyVar<Executor> = ThreadSafeLazyVar {
//    val count = AtomicInteger(0)
//    val executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), {
//        val thread = Thread(it)
//        thread.setDaemon(true)
//        thread.setName("komponents-promises-${count.incrementAndGet()}")
//        thread
//    })
//
//    Runtime.getRuntime().addShutdownHook(Thread() {
//        executorService.shutdown()
//        executorService.awaitTermination(60, TimeUnit.SECONDS)
//    })
//    executorService
//}
