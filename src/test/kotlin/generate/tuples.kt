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
        (1..i).forEach {
            print("V$it")
            if (it < i) print(", ")
        }
        println(">")

        print("(")
        (1..i).forEach {
            print("public val ${propertyNames[it-1]} : V$it")
            if (it < i) println(",")
        }
        println(")")
        println()
    }
}

val propertyNames = listOf(
        "first",
        "second",
        "third",
        "fourth",
        "fifth",
        "sixth",
        "seventh",
        "eighth",
        "ninth",
        "tenth",
        "eleventh",
        "twelfth",
        "thirteenth",
        "fourteenth",
        "fifteenth",
        "sixteenth",
        "seventeenth",
        "eighteenth",
        "nineteenth",
        "twentieth")