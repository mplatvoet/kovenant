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

package generate.combine


fun main(args: Array<String>) {
    generateApiCombine(20)
//    generateConcreteCombine(20)

}

/*

[@Suppress("UNCHECKED_CAST")]
public fun concreteCombine<V1, V2, E>
        (p1: Promise<V1, E>,
         p2: Promise<V2, E>): Promise<Tuple2<V1, V2>, E> {
    val deferred = deferred<Tuple2<V1, V2>, E>()

    val results = AtomicReferenceArray<Any?>(2)
    val successCount = AtomicInteger(2)

    fun createTuple() : Tuple2<V1, V2> {
        return Tuple2(
                results[0] as V1,
                results[1] as V2)
    }

    fun Promise<*,*>.registerSuccess( idx: Int) {
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
 */

fun generateConcreteCombine(n: Int) {
    (2..n).forEach { i ->
        println("@Suppress(\"UNCHECKED_CAST\")")
        print("fun <")
        (1..i).forEach {
            print("V$it, ")
        }
        println("E> concreteCombine")

        print("(")
        (1..i).forEach {
            print("p$it : Promise<V$it, E>")
            if (it < i) println(",")
        }
        print(") : Promise<Tuple$i<")
        (1..i).forEach {
            print("V$it")
            if (it < i) print(", ")
        }
        println(">, E> {")
        print("val deferred = deferred<Tuple$i<")
        (1..i).forEach {
            print("V$it")
            if (it < i) print(", ")
        }
        println(">, E>()")
        println()
        println("val results = AtomicReferenceArray<Any?>($i)")
        println("val successCount = AtomicInteger($i)")
        println()
        print("fun createTuple() : Tuple$i<")
        (1..i).forEach {
            print("V$it")
            if (it < i) print(", ")
        }
        println("> {")
        println("return Tuple$i(")
        (1..i).forEach {
            print("results.get(${it - 1}) as V$it")
            if (it < i) println(", ")
        }
        println(")")
        println("}")
        println("""
     fun Promise<*,*>.registerSuccess( idx: Int) {
        success { v ->
            results.set(idx, v)
            if (successCount.decrementAndGet() == 0) {
                deferred.resolve(createTuple())
            }
        }
    }
    """)
        print("deferred.registerFail(")
        (1..i).forEach {
            print("p$it")
            if (it < i) print(", ")
        }
        println(")")
        (1..i).forEach {
            println("p$it.registerSuccess(${it - 1})")
        }

        println()

        println("return deferred.promise")
        println("}")
    }
}

/*

public fun combine
        <V1, V2, E>
        (p1 : Promise<V1, E>,
         p2 : Promise<V2, E>) : Promise<Tuple2<V1, V2>, E>
= concreteCombine(p1, p2)
 */
fun generateApiCombine(n: Int) {
    (2..n).forEach { i ->
        print("public fun <")
        (1..i).forEach {
            print("V$it, ")
        }
        println("E> combine")

        print("(")
        (1..i).forEach {
            print("p$it : Promise<V$it, E>")
            if (it < i) println(",")
        }
        print(") : Promise<Tuple$i<")
        (1..i).forEach {
            print("V$it")
            if (it < i) print(", ")
        }
        println(">, E>")
        print("= concreteCombine(")
        (1..i).forEach {
            print("p$it")
            if (it < i) print(", ")
        }
        println(")")
    }
}

