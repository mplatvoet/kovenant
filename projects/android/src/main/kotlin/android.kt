package nl.mplatvoet.komponents.kovenant.android

import android.os.Handler
import android.os.Looper
import android.os.Message
import nl.mplatvoet.komponents.kovenant.Context
import nl.mplatvoet.komponents.kovenant.ContextAware
import nl.mplatvoet.komponents.kovenant.Kovenant
import nl.mplatvoet.komponents.kovenant.Promise

public fun <V, E> Promise<V, E>.successUI(body: (value: V) -> Unit): Promise<V, E> = success {
    UIThreadRunner.submit(Param1Callback(ctx, it, body))
}

public fun <V, E> Promise<V, E>.failUI(body: (error: E) -> Unit): Promise<V, E> = fail {
    UIThreadRunner.submit(Param1Callback(ctx, it, body))
}

public fun <V, E> Promise<V, E>.alwaysUI(body: () -> Unit): Promise<V, E> = always {
    UIThreadRunner.submit(Param0Callback(ctx, body))
}

private trait Callback {
    val context: Context
    fun execute()
}

private class Param1Callback<V>(override val context :Context, private val value: V, private val body: (value: V) -> Unit) : Callback {
    override fun execute() = body(value)
}

private class Param0Callback(override val context :Context, private val body: () -> Unit) : Callback {
    override fun execute() = body()
}

private object UIThreadRunner : Handler.Callback {
    private val handler = Handler(Looper.getMainLooper())

    override fun handleMessage(msg: Message): Boolean {
        val callback = msg.obj as Callback
        try {
            callback.execute()
        } catch(e:Exception) {
            callback.context.callbackError(e)
        } finally {
            //not necessary but let's clean quick and swift
            msg.recycle()
        }
        return true // signal message was handled
    }

    public fun submit(callback: Callback) {
        val message = handler.obtainMessage(-1, callback)
        handler.dispatchMessage(message)
    }
}

private val <V, E> Promise<V, E>.ctx: Context
    get() = when(this) {
        is ContextAware -> this.context
        else -> Kovenant.context
    }