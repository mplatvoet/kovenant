package nl.komponents.kovenant.unsafe

import sun.misc.Unsafe
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import kotlin.reflect.KClass

class UnsafeAtomicReferenceFieldUpdater<C : Any, V : Any>(targetClass: KClass<C>,
                                                          fieldName: String) : AtomicReferenceFieldUpdater<C, V>() {
    companion object {
        private val unsafe = getUnsafe()
    }

    private val offset: Long

    init {
        val field = targetClass.java.getDeclaredField(fieldName)
        offset = unsafe.objectFieldOffset(field)
    }

    override fun lazySet(target: C, newValue: V?) = unsafe.putOrderedObject(target, offset, newValue)
    override fun compareAndSet(target: C, expected: V?, update: V?): Boolean = unsafe.compareAndSwapObject(target, offset, expected, update)

    override fun set(target: C, newValue: V?) = unsafe.putObjectVolatile(target, offset, newValue);

    @Suppress("UNCHECKED_CAST")
    override fun get(target: C): V? = unsafe.getObjectVolatile(target, offset) as V

    //Equals AtomicReferenceFieldUpdater implementation on Java 8
    override fun weakCompareAndSet(target: C, expected: V?, update: V?): Boolean = compareAndSet(target, expected, update)
}


fun hasUnsafe(): Boolean {
    try {
        Class.forName("sun.misc.Unsafe")
        return true
    } catch(e: ClassNotFoundException) {
        return false
    }
}

private fun getUnsafe(): Unsafe {
    try {
        val field = Unsafe::class.java.getDeclaredField("theUnsafe");
        field.isAccessible = true;
        return field.get(null) as Unsafe;

    } catch (e: Exception) {
        throw RuntimeException("unsafe doesn't exist or is not accessible")
    }
}