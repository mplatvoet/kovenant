package nl.mplatvoet.kotlin.komponents.promises;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
* Created by mplatvoet on 30-12-2014.
*/
final class UnsafeAccess {
    private static final Unsafe UNSAFE = retrieveUnsafe();

    private UnsafeAccess() {}

    @SuppressWarnings("restriction")
    private static Unsafe retrieveUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    static <T> long objectFieldOffset(Class<T> clazz, String fieldName) {
        try {
            return  UNSAFE.objectFieldOffset(clazz.getDeclaredField(fieldName));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    static boolean compareAndSwapObject(Object obj, long fieldOffset, Object expected, Object newValue) {
        return UNSAFE.compareAndSwapObject(obj, fieldOffset, expected, newValue);
    }

    static boolean compareAndSwapInt(Object obj, long fieldOffset, int expected, int newValue) {
        return UNSAFE.compareAndSwapInt(obj, fieldOffset, expected, newValue);
    }
}
