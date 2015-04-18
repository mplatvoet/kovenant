package generate.combine


fun main(args: Array<String>) {
    generateApiCombine(20)
//    generateConcreteCombine(20)

}

/*

[suppress("UNCHECKED_CAST")]
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
    (2..n) forEach { i ->
        println("[suppress(\"UNCHECKED_CAST\")]")
        print("fun concreteCombine<")
        (1..i).forEach {
            print("V$it, ")
        }
        println("E>")

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
            print("results[${it - 1}] as V$it")
            if (it < i) println(", ")
        }
        println(")")
        println("}")
        println("""
         fun Promise<*,*>.registerSuccess( idx: Int) {
            success { v ->
                results[idx] = v
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
            println("p$it registerSuccess ${it - 1}")
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
    (2..n) forEach { i ->
        println("public fun combine")
        print("<")
        (1..i).forEach {
            print("V$it, ")
        }
        println("E>")

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

