package nl.mplatvoet.kotlin.komponents.promises

import kotlin.properties.ReadWriteProperty
import kotlin.properties.ReadOnlyProperty
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by mplatvoet on 30-5-2014.
 */



[suppress("UNCHECKED_CAST")]
public class ThreadSafeLazyVar<T>(private val initializer: () -> T) : ReadWriteProperty<Any?, T> {
    private val lock = Any()
    private volatile var value: Any? = null

    public override fun get(thisRef: Any?, desc: PropertyMetadata): T {
        val curVal = value
        return if (curVal != null) {
            unmask(curVal) as T
        } else synchronized(lock) {
            val newVal = initializer()
            value = mask(newVal)
            newVal
        }
    }

    public override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        this.value = mask(value)
    }

    val initialized: Boolean get() = value != null
}

public fun writeOnceVar<T>(onValueSet: (T) -> Unit = { }): ReadWriteProperty<Any?, T> = WriteOnceVar<T>(onValueSet)

[suppress("UNCHECKED_CAST")]
private class WriteOnceVar<T>(private val onValueSet: (T) -> Unit) : ReadWriteProperty<Any?, T> {
    private val value = AtomicReference<T>()

    public override fun get(thisRef: Any?, desc: PropertyMetadata): T = unmask(value.get()) as T

    public override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        if (this.value.compareAndSet(null, mask(value) as T)) {
            onValueSet(value)
        } else {
            throw IllegalStateException("value can only be set once")
        }
    }
}

public fun nonBlockingLazyVal<T>(initializer: () -> T): ReadOnlyProperty<Any?, T> = NonBlockingLazyVal<T>(initializer)

[suppress("UNCHECKED_CAST")]
private class NonBlockingLazyVal<T>(private val initializer: () -> T) : ReadOnlyProperty<Any?, T> {
    private val value = AtomicReference<T>()

    public override fun get(thisRef: Any?, desc: PropertyMetadata): T {
        val curVal = value.get()
        return if (curVal != null) {
            unmask(curVal) as T
        } else {
            //allows duplicate initialization but only 1 will ever succeed
            val newVal = initializer()
            if (value.compareAndSet(null, mask(newVal) as T)) {
                newVal
            } else {
                unmask(value.get()) as T
            }
        }
    }
}

[suppress("UNCHECKED_CAST")]
private class TrackChangesVar<T>(private val source: () -> T) : ReadWriteProperty<Any?, T> {
    private volatile var value: Any? = null

    public override fun get(thisRef: Any?, desc: PropertyMetadata): T {
        val curVal = value
        return if (curVal != null) unmask(curVal) as T else source()
    }

    public override fun set(thisRef: Any?, desc: PropertyMetadata, value: T) {
        this.value = mask(value)
    }

    val written: Boolean get() = value != null
}

