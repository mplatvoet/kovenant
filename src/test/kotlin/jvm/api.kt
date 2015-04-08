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
        var called = 0
        Kovenant.async {1+1}.success { called++ }
        assertEquals(1, called, "success should be called once")
    }

    test fun testFail() {
        var called = 0
        Kovenant.async {throw Exception("catch me if you can")}.fail { called++ }
        assertEquals(1, called, "fail should be called")
    }

    test fun testAlwaysOnSuccess() {
        var called = 0
        Kovenant.async {1+1}.always { called++ }
        assertEquals(1, called, "always should be called")
    }

    test fun testAlwaysOnFail() {
        var called = 0
        Kovenant.async {throw Exception("catch me if you can")}.always { called++ }
        assertEquals(1, called, "always should be called")
    }
}

