/*
 * Copyright (c) 2015 Mark Platvoet<mplatvoet@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * THE SOFTWARE.
 */

package nl.komponents.kovenant.combine

import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceArray


@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>): Promise<Tuple2<V1, V2>, E> {
    val deferred = deferred<Tuple2<V1, V2>, E>()

    val results = AtomicReferenceArray<Any?>(2)
    val successCount = AtomicInteger(2)

    fun createTuple(): Tuple2<V1, V2> {
        return Tuple2(
                results[0] as V1,
                results[1] as V2)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2)
    p1 registerSuccess 0
    p2 registerSuccess 1

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>): Promise<Tuple3<V1, V2, V3>, E> {
    val deferred = deferred<Tuple3<V1, V2, V3>, E>()

    val results = AtomicReferenceArray<Any?>(3)
    val successCount = AtomicInteger(3)

    fun createTuple(): Tuple3<V1, V2, V3> {
        return Tuple3(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>): Promise<Tuple4<V1, V2, V3, V4>, E> {
    val deferred = deferred<Tuple4<V1, V2, V3, V4>, E>()

    val results = AtomicReferenceArray<Any?>(4)
    val successCount = AtomicInteger(4)

    fun createTuple(): Tuple4<V1, V2, V3, V4> {
        return Tuple4(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>): Promise<Tuple5<V1, V2, V3, V4, V5>, E> {
    val deferred = deferred<Tuple5<V1, V2, V3, V4, V5>, E>()

    val results = AtomicReferenceArray<Any?>(5)
    val successCount = AtomicInteger(5)

    fun createTuple(): Tuple5<V1, V2, V3, V4, V5> {
        return Tuple5(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>): Promise<Tuple6<V1, V2, V3, V4, V5, V6>, E> {
    val deferred = deferred<Tuple6<V1, V2, V3, V4, V5, V6>, E>()

    val results = AtomicReferenceArray<Any?>(6)
    val successCount = AtomicInteger(6)

    fun createTuple(): Tuple6<V1, V2, V3, V4, V5, V6> {
        return Tuple6(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>): Promise<Tuple7<V1, V2, V3, V4, V5, V6, V7>, E> {
    val deferred = deferred<Tuple7<V1, V2, V3, V4, V5, V6, V7>, E>()

    val results = AtomicReferenceArray<Any?>(7)
    val successCount = AtomicInteger(7)

    fun createTuple(): Tuple7<V1, V2, V3, V4, V5, V6, V7> {
        return Tuple7(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>): Promise<Tuple8<V1, V2, V3, V4, V5, V6, V7, V8>, E> {
    val deferred = deferred<Tuple8<V1, V2, V3, V4, V5, V6, V7, V8>, E>()

    val results = AtomicReferenceArray<Any?>(8)
    val successCount = AtomicInteger(8)

    fun createTuple(): Tuple8<V1, V2, V3, V4, V5, V6, V7, V8> {
        return Tuple8(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>): Promise<Tuple9<V1, V2, V3, V4, V5, V6, V7, V8, V9>, E> {
    val deferred = deferred<Tuple9<V1, V2, V3, V4, V5, V6, V7, V8, V9>, E>()

    val results = AtomicReferenceArray<Any?>(9)
    val successCount = AtomicInteger(9)

    fun createTuple(): Tuple9<V1, V2, V3, V4, V5, V6, V7, V8, V9> {
        return Tuple9(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>): Promise<Tuple10<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10>, E> {
    val deferred = deferred<Tuple10<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10>, E>()

    val results = AtomicReferenceArray<Any?>(10)
    val successCount = AtomicInteger(10)

    fun createTuple(): Tuple10<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10> {
        return Tuple10(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>): Promise<Tuple11<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11>, E> {
    val deferred = deferred<Tuple11<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11>, E>()

    val results = AtomicReferenceArray<Any?>(11)
    val successCount = AtomicInteger(11)

    fun createTuple(): Tuple11<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11> {
        return Tuple11(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, V12 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>,
         p12: Promise<V12, E>): Promise<Tuple12<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12>, E> {
    val deferred = deferred<Tuple12<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12>, E>()

    val results = AtomicReferenceArray<Any?>(12)
    val successCount = AtomicInteger(12)

    fun createTuple(): Tuple12<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12> {
        return Tuple12(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11,
                results[11] as V12)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10
    p12 registerSuccess 11

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, V12 : Any, V13 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>,
         p12: Promise<V12, E>,
         p13: Promise<V13, E>): Promise<Tuple13<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13>, E> {
    val deferred = deferred<Tuple13<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13>, E>()

    val results = AtomicReferenceArray<Any?>(13)
    val successCount = AtomicInteger(13)

    fun createTuple(): Tuple13<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13> {
        return Tuple13(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11,
                results[11] as V12,
                results[12] as V13)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10
    p12 registerSuccess 11
    p13 registerSuccess 12

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, V12 : Any, V13 : Any, V14 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>,
         p12: Promise<V12, E>,
         p13: Promise<V13, E>,
         p14: Promise<V14, E>): Promise<Tuple14<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14>, E> {
    val deferred = deferred<Tuple14<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14>, E>()

    val results = AtomicReferenceArray<Any?>(14)
    val successCount = AtomicInteger(14)

    fun createTuple(): Tuple14<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14> {
        return Tuple14(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11,
                results[11] as V12,
                results[12] as V13,
                results[13] as V14)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10
    p12 registerSuccess 11
    p13 registerSuccess 12
    p14 registerSuccess 13

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, V12 : Any, V13 : Any, V14 : Any, V15 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>,
         p12: Promise<V12, E>,
         p13: Promise<V13, E>,
         p14: Promise<V14, E>,
         p15: Promise<V15, E>): Promise<Tuple15<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15>, E> {
    val deferred = deferred<Tuple15<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15>, E>()

    val results = AtomicReferenceArray<Any?>(15)
    val successCount = AtomicInteger(15)

    fun createTuple(): Tuple15<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15> {
        return Tuple15(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11,
                results[11] as V12,
                results[12] as V13,
                results[13] as V14,
                results[14] as V15)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10
    p12 registerSuccess 11
    p13 registerSuccess 12
    p14 registerSuccess 13
    p15 registerSuccess 14

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, V12 : Any, V13 : Any, V14 : Any, V15 : Any, V16 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>,
         p12: Promise<V12, E>,
         p13: Promise<V13, E>,
         p14: Promise<V14, E>,
         p15: Promise<V15, E>,
         p16: Promise<V16, E>): Promise<Tuple16<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16>, E> {
    val deferred = deferred<Tuple16<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16>, E>()

    val results = AtomicReferenceArray<Any?>(16)
    val successCount = AtomicInteger(16)

    fun createTuple(): Tuple16<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16> {
        return Tuple16(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11,
                results[11] as V12,
                results[12] as V13,
                results[13] as V14,
                results[14] as V15,
                results[15] as V16)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10
    p12 registerSuccess 11
    p13 registerSuccess 12
    p14 registerSuccess 13
    p15 registerSuccess 14
    p16 registerSuccess 15

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, V12 : Any, V13 : Any, V14 : Any, V15 : Any, V16 : Any, V17 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>,
         p12: Promise<V12, E>,
         p13: Promise<V13, E>,
         p14: Promise<V14, E>,
         p15: Promise<V15, E>,
         p16: Promise<V16, E>,
         p17: Promise<V17, E>): Promise<Tuple17<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17>, E> {
    val deferred = deferred<Tuple17<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17>, E>()

    val results = AtomicReferenceArray<Any?>(17)
    val successCount = AtomicInteger(17)

    fun createTuple(): Tuple17<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17> {
        return Tuple17(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11,
                results[11] as V12,
                results[12] as V13,
                results[13] as V14,
                results[14] as V15,
                results[15] as V16,
                results[16] as V17)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10
    p12 registerSuccess 11
    p13 registerSuccess 12
    p14 registerSuccess 13
    p15 registerSuccess 14
    p16 registerSuccess 15
    p17 registerSuccess 16

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, V12 : Any, V13 : Any, V14 : Any, V15 : Any, V16 : Any, V17 : Any, V18 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>,
         p12: Promise<V12, E>,
         p13: Promise<V13, E>,
         p14: Promise<V14, E>,
         p15: Promise<V15, E>,
         p16: Promise<V16, E>,
         p17: Promise<V17, E>,
         p18: Promise<V18, E>): Promise<Tuple18<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18>, E> {
    val deferred = deferred<Tuple18<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18>, E>()

    val results = AtomicReferenceArray<Any?>(18)
    val successCount = AtomicInteger(18)

    fun createTuple(): Tuple18<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18> {
        return Tuple18(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11,
                results[11] as V12,
                results[12] as V13,
                results[13] as V14,
                results[14] as V15,
                results[15] as V16,
                results[16] as V17,
                results[17] as V18)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10
    p12 registerSuccess 11
    p13 registerSuccess 12
    p14 registerSuccess 13
    p15 registerSuccess 14
    p16 registerSuccess 15
    p17 registerSuccess 16
    p18 registerSuccess 17

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, V12 : Any, V13 : Any, V14 : Any, V15 : Any, V16 : Any, V17 : Any, V18 : Any, V19 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>,
         p12: Promise<V12, E>,
         p13: Promise<V13, E>,
         p14: Promise<V14, E>,
         p15: Promise<V15, E>,
         p16: Promise<V16, E>,
         p17: Promise<V17, E>,
         p18: Promise<V18, E>,
         p19: Promise<V19, E>): Promise<Tuple19<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19>, E> {
    val deferred = deferred<Tuple19<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19>, E>()

    val results = AtomicReferenceArray<Any?>(19)
    val successCount = AtomicInteger(19)

    fun createTuple(): Tuple19<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19> {
        return Tuple19(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11,
                results[11] as V12,
                results[12] as V13,
                results[13] as V14,
                results[14] as V15,
                results[15] as V16,
                results[16] as V17,
                results[17] as V18,
                results[18] as V19)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10
    p12 registerSuccess 11
    p13 registerSuccess 12
    p14 registerSuccess 13
    p15 registerSuccess 14
    p16 registerSuccess 15
    p17 registerSuccess 16
    p18 registerSuccess 17
    p19 registerSuccess 18

    return deferred.promise
}

@suppress("UNCHECKED_CAST")
fun concreteCombine<V1 : Any, V2 : Any, V3 : Any, V4 : Any, V5 : Any, V6 : Any, V7 : Any, V8 : Any, V9 : Any, V10 : Any, V11 : Any, V12 : Any, V13 : Any, V14 : Any, V15 : Any, V16 : Any, V17 : Any, V18 : Any, V19 : Any, V20 : Any, E : Any>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>,
         p3: Promise<V3, E>,
         p4: Promise<V4, E>,
         p5: Promise<V5, E>,
         p6: Promise<V6, E>,
         p7: Promise<V7, E>,
         p8: Promise<V8, E>,
         p9: Promise<V9, E>,
         p10: Promise<V10, E>,
         p11: Promise<V11, E>,
         p12: Promise<V12, E>,
         p13: Promise<V13, E>,
         p14: Promise<V14, E>,
         p15: Promise<V15, E>,
         p16: Promise<V16, E>,
         p17: Promise<V17, E>,
         p18: Promise<V18, E>,
         p19: Promise<V19, E>,
         p20: Promise<V20, E>): Promise<Tuple20<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, V20>, E> {
    val deferred = deferred<Tuple20<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, V20>, E>()

    val results = AtomicReferenceArray<Any?>(20)
    val successCount = AtomicInteger(20)

    fun createTuple(): Tuple20<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, V20> {
        return Tuple20(
                results[0] as V1,
                results[1] as V2,
                results[2] as V3,
                results[3] as V4,
                results[4] as V5,
                results[5] as V6,
                results[6] as V7,
                results[7] as V8,
                results[8] as V9,
                results[9] as V10,
                results[10] as V11,
                results[11] as V12,
                results[12] as V13,
                results[13] as V14,
                results[14] as V15,
                results[15] as V16,
                results[16] as V17,
                results[17] as V18,
                results[18] as V19,
                results[19] as V20)
    }

    fun Promise<*, *>.registerSuccess(idx: Int) {
        success { v ->
            results[idx] = v
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }

    deferred.registerFail(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20)
    p1 registerSuccess 0
    p2 registerSuccess 1
    p3 registerSuccess 2
    p4 registerSuccess 3
    p5 registerSuccess 4
    p6 registerSuccess 5
    p7 registerSuccess 6
    p8 registerSuccess 7
    p9 registerSuccess 8
    p10 registerSuccess 9
    p11 registerSuccess 10
    p12 registerSuccess 11
    p13 registerSuccess 12
    p14 registerSuccess 13
    p15 registerSuccess 14
    p16 registerSuccess 15
    p17 registerSuccess 16
    p18 registerSuccess 17
    p19 registerSuccess 18
    p20 registerSuccess 19

    return deferred.promise
}


private fun <V : Any, E : Any>Deferred<V, E>.registerFail(vararg promises: Promise<*, E>) {
    val failCount = AtomicInteger(0)
    promises.forEach { promise ->
        promise.fail { e ->
            if (failCount.incrementAndGet() == 1) {
                this.reject(e)
            }
        }
    }
}








