package generate.tuples

/*
public data class Tuple2
<V1, V2>
(val first: V1,
 val second: V2)
 */

fun main(args: Array<String>) {
    (2..20) forEach { i ->
        println("public data class Tuple$i")

        print("<")
        (1..i) .forEach {
            print("V$it")
            if (it < i) print(", ")
        }
        println(">")

        print("(")
        (1..i) .forEach {
            print("val value$it : V$it")
            if (it < i) println(",")
        }
        println(")")
        println()
    }
}



