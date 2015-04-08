package nl.mplatvoet.komponents.kovenant

import org.junit.Before as before
import org.junit.Test as test
import kotlin.test.assertEquals

public class APITest {

    class CurrentThreadDispatcher : Dispatcher {
        override fun submit(task: () -> Unit) = task()
    }

    before fun setUp() {
        Kovenant.configure {
            callbackDispatcher = CurrentThreadDispatcher()
            workerDispatcher = CurrentThreadDispatcher()
        }
    }



    test fun testSuccess() {
        var called = false
        Kovenant.async {1+1}.success { called = true }
        assertEquals(true, called, "succes should be called")
    }
}

