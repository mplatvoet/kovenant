package nl.komponents.kovenant.rx

import nl.komponents.kovenant.Dispatcher
import nl.komponents.kovenant.NonBlockingWorkQueue
import rx.Scheduler
import rx.Subscription
import rx.functions.Action0
import java.util.concurrent.TimeUnit

class DispatcherScheduler(private val target: Dispatcher) : Dispatcher by target, Scheduler() {
    override fun createWorker(): Worker = DispatcherSchedulerWorker()

    private inner class DispatcherSchedulerWorker : Worker() {
        private val workQueue = NonBlockingWorkQueue<Action0>()

        override fun schedule(action: Action0): Subscription {
            throw UnsupportedOperationException()
        }

        override fun schedule(action: Action0, amount: Long, unit: TimeUnit): Subscription {
            throw UnsupportedOperationException()
        }

        override fun isUnsubscribed(): Boolean {
            throw UnsupportedOperationException()
        }

        override fun unsubscribe() {
            throw UnsupportedOperationException()
        }
    }
}

