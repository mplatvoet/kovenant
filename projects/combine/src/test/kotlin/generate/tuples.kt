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