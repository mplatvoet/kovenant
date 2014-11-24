package nl.mplatvoet.kotlin.komponents.promises

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.TimeUnit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by mplatvoet on 30-5-2014.
 */





public object Promises {
    val configuration: Configuration
        get() = mutableConfiguration.get()!!

    private var mutableConfiguration = AtomicReference(ThreadSafeConfiguration())

    public fun configure(body: MutableConfiguration.() -> Unit) {
        //a copy-on-write strategy is used, but in order to maintain the lazy loading mechanism
        //keeping track of what the developer actually altered is needed, otherwise
        //everything gets initialized during configuration
        val trackingConfiguration = TrackingConfiguration(mutableConfiguration.get()!!)
        trackingConfiguration.body()

        do {
            val current = mutableConfiguration.get()!!
            val newConfig = current.copy()
            trackingConfiguration.applyChanged(newConfig)
        } while (!mutableConfiguration.compareAndSet(current, newConfig))
    }

    private class ThreadSafeConfiguration() : MutableConfiguration {
        override var fallbackOnCurrentThread: Boolean = true

        private val executionErrorsDelegate = ThreadSafeLazyVar {
            {(e: Exception) -> e.printStackTrace() }
        }
        override var executionErrors: (Exception) -> Unit by executionErrorsDelegate

        private val multipleCompletionDelegate = ThreadSafeLazyVar<(Any, Any) -> Unit> {
            {(curVal:Any, newVal:Any)-> throw IllegalStateException("Value[$curVal] is set, can't override with new value[$newVal]") }
        }
        override var multipleCompletion: (curVal:Any, newVal:Any) -> Unit by multipleCompletionDelegate


        private val executorDelegate: ThreadSafeLazyVar<Executor> = ThreadSafeLazyVar {
            val count = AtomicInteger(0)
            val executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), {
                val thread = Thread(it)
                thread.setDaemon(true)
                thread.setName("komponents-promises-${count.incrementAndGet()}")
                thread
            })

            Runtime.getRuntime().addShutdownHook(Thread() {
                executorService.shutdown()
                executorService.awaitTermination(60, TimeUnit.SECONDS)
            })
            executorService
        }
        override var executor: Executor by executorDelegate

        fun copy(): ThreadSafeConfiguration {
            val copy = ThreadSafeConfiguration()
            copy.fallbackOnCurrentThread = fallbackOnCurrentThread
            if (executionErrorsDelegate.initialized) copy.executionErrors = executionErrors
            if (executorDelegate.initialized) copy.executor = executor
            if (multipleCompletionDelegate.initialized) copy.multipleCompletion = multipleCompletion
            return copy
        }
    }

    private class TrackingConfiguration(private val currentConfig: Configuration) : MutableConfiguration {
        private val fallbackOnCurrentThreadDelegate = TrackChangesVar { currentConfig.fallbackOnCurrentThread }
        override var fallbackOnCurrentThread: Boolean by fallbackOnCurrentThreadDelegate

        private val executorDelegate = TrackChangesVar { currentConfig.executor }
        override var executor: Executor by executorDelegate

        private val executionErrorsDelegate = TrackChangesVar { currentConfig.executionErrors }
        override var executionErrors: (Exception) -> Unit by executionErrorsDelegate

        private val multipleCompletionDelegate = TrackChangesVar { currentConfig.multipleCompletion }
        override var multipleCompletion: (curVal:Any, newVal:Any) -> Unit by multipleCompletionDelegate

        fun applyChanged(config: MutableConfiguration) {
            if (fallbackOnCurrentThreadDelegate.written)
                config.fallbackOnCurrentThread = fallbackOnCurrentThread

            if (executorDelegate.written)
                config.executor = executor

            if (executionErrorsDelegate.written)
                config.executionErrors = executionErrors
        }
    }
}

public trait Configuration {
    val fallbackOnCurrentThread: Boolean
    val executor: Executor
    val executionErrors: (Exception) -> Unit
    val multipleCompletion: (curVal:Any, newVal:Any) -> Unit
}

public trait MutableConfiguration : Configuration {
    override var fallbackOnCurrentThread: Boolean
    override var executor: Executor
    override var executionErrors: (Exception) -> Unit
    override var multipleCompletion: (curVal:Any, newVal:Any) -> Unit
}