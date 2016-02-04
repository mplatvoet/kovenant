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
@file:JvmName("KovenantCombineApi")
package nl.komponents.kovenant.combine

import nl.komponents.kovenant.Promise


infix fun <V1, V2, E> Promise<V1, E>.and(other: Promise<V2, E>): Promise<Tuple2<V1, V2>, E> = combine(this, other)

fun <V1, V2, E> combine(p1: Promise<V1, E>,
                               p2: Promise<V2, E>): Promise<Tuple2<V1, V2>, E>
        = concreteCombine(p1, p2)

fun <V1, V2, V3, E> combine(p1: Promise<V1, E>,
                                   p2: Promise<V2, E>,
                                   p3: Promise<V3, E>): Promise<Tuple3<V1, V2, V3>, E>
        = concreteCombine(p1, p2, p3)

fun <V1, V2, V3, V4, E> combine(p1: Promise<V1, E>,
                                       p2: Promise<V2, E>,
                                       p3: Promise<V3, E>,
                                       p4: Promise<V4, E>): Promise<Tuple4<V1, V2, V3, V4>, E>
        = concreteCombine(p1, p2, p3, p4)

fun <V1, V2, V3, V4, V5, E> combine(p1: Promise<V1, E>,
                                           p2: Promise<V2, E>,
                                           p3: Promise<V3, E>,
                                           p4: Promise<V4, E>,
                                           p5: Promise<V5, E>): Promise<Tuple5<V1, V2, V3, V4, V5>, E>
        = concreteCombine(p1, p2, p3, p4, p5)

fun <V1, V2, V3, V4, V5, V6, E> combine(p1: Promise<V1, E>,
                                               p2: Promise<V2, E>,
                                               p3: Promise<V3, E>,
                                               p4: Promise<V4, E>,
                                               p5: Promise<V5, E>,
                                               p6: Promise<V6, E>): Promise<Tuple6<V1, V2, V3, V4, V5, V6>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6)

fun <V1, V2, V3, V4, V5, V6, V7, E> combine(p1: Promise<V1, E>,
                                                   p2: Promise<V2, E>,
                                                   p3: Promise<V3, E>,
                                                   p4: Promise<V4, E>,
                                                   p5: Promise<V5, E>,
                                                   p6: Promise<V6, E>,
                                                   p7: Promise<V7, E>): Promise<Tuple7<V1, V2, V3, V4, V5, V6, V7>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7)

fun <V1, V2, V3, V4, V5, V6, V7, V8, E> combine(p1: Promise<V1, E>,
                                                       p2: Promise<V2, E>,
                                                       p3: Promise<V3, E>,
                                                       p4: Promise<V4, E>,
                                                       p5: Promise<V5, E>,
                                                       p6: Promise<V6, E>,
                                                       p7: Promise<V7, E>,
                                                       p8: Promise<V8, E>): Promise<Tuple8<V1, V2, V3, V4, V5, V6, V7, V8>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, E> combine(p1: Promise<V1, E>,
                                                           p2: Promise<V2, E>,
                                                           p3: Promise<V3, E>,
                                                           p4: Promise<V4, E>,
                                                           p5: Promise<V5, E>,
                                                           p6: Promise<V6, E>,
                                                           p7: Promise<V7, E>,
                                                           p8: Promise<V8, E>,
                                                           p9: Promise<V9, E>): Promise<Tuple9<V1, V2, V3, V4, V5, V6, V7, V8, V9>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, E> combine(p1: Promise<V1, E>,
                                                                p2: Promise<V2, E>,
                                                                p3: Promise<V3, E>,
                                                                p4: Promise<V4, E>,
                                                                p5: Promise<V5, E>,
                                                                p6: Promise<V6, E>,
                                                                p7: Promise<V7, E>,
                                                                p8: Promise<V8, E>,
                                                                p9: Promise<V9, E>,
                                                                p10: Promise<V10, E>): Promise<Tuple10<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, E> combine(p1: Promise<V1, E>,
                                                                     p2: Promise<V2, E>,
                                                                     p3: Promise<V3, E>,
                                                                     p4: Promise<V4, E>,
                                                                     p5: Promise<V5, E>,
                                                                     p6: Promise<V6, E>,
                                                                     p7: Promise<V7, E>,
                                                                     p8: Promise<V8, E>,
                                                                     p9: Promise<V9, E>,
                                                                     p10: Promise<V10, E>,
                                                                     p11: Promise<V11, E>): Promise<Tuple11<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, E> combine(p1: Promise<V1, E>,
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
                                                                          p12: Promise<V12, E>): Promise<Tuple12<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, E> combine(p1: Promise<V1, E>,
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
                                                                               p13: Promise<V13, E>): Promise<Tuple13<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, E> combine(p1: Promise<V1, E>,
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
                                                                                    p14: Promise<V14, E>): Promise<Tuple14<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, E> combine(p1: Promise<V1, E>,
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
                                                                                         p15: Promise<V15, E>): Promise<Tuple15<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, E> combine(p1: Promise<V1, E>,
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
                                                                                              p16: Promise<V16, E>): Promise<Tuple16<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, E> combine(p1: Promise<V1, E>,
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
                                                                                                   p17: Promise<V17, E>): Promise<Tuple17<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, E> combine(p1: Promise<V1, E>,
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
                                                                                                        p18: Promise<V18, E>): Promise<Tuple18<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, E> combine(p1: Promise<V1, E>,
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
                                                                                                             p19: Promise<V19, E>): Promise<Tuple19<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19)

fun <V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, V20, E> combine(p1: Promise<V1, E>,
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
                                                                                                                  p20: Promise<V20, E>): Promise<Tuple20<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, V20>, E>
        = concreteCombine(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20)

data class Tuple2
<V1, V2>
(val first: V1,
 val second: V2)

data class Tuple3
<V1, V2, V3>
(val first: V1,
 val second: V2,
 val third: V3)

data class Tuple4
<V1, V2, V3, V4>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4)

data class Tuple5
<V1, V2, V3, V4, V5>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5)

data class Tuple6
<V1, V2, V3, V4, V5, V6>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6)

data class Tuple7
<V1, V2, V3, V4, V5, V6, V7>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7)

data class Tuple8
<V1, V2, V3, V4, V5, V6, V7, V8>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8)

data class Tuple9
<V1, V2, V3, V4, V5, V6, V7, V8, V9>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9)

data class Tuple10
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10)

data class Tuple11
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11)

data class Tuple12
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11,
 val twelfth: V12)

data class Tuple13
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11,
 val twelfth: V12,
 val thirteenth: V13)

data class Tuple14
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11,
 val twelfth: V12,
 val thirteenth: V13,
 val fourteenth: V14)

data class Tuple15
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11,
 val twelfth: V12,
 val thirteenth: V13,
 val fourteenth: V14,
 val fifteenth: V15)

data class Tuple16
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11,
 val twelfth: V12,
 val thirteenth: V13,
 val fourteenth: V14,
 val fifteenth: V15,
 val sixteenth: V16)

data class Tuple17
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11,
 val twelfth: V12,
 val thirteenth: V13,
 val fourteenth: V14,
 val fifteenth: V15,
 val sixteenth: V16,
 val seventeenth: V17)

data class Tuple18
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11,
 val twelfth: V12,
 val thirteenth: V13,
 val fourteenth: V14,
 val fifteenth: V15,
 val sixteenth: V16,
 val seventeenth: V17,
 val eighteenth: V18)

data class Tuple19
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11,
 val twelfth: V12,
 val thirteenth: V13,
 val fourteenth: V14,
 val fifteenth: V15,
 val sixteenth: V16,
 val seventeenth: V17,
 val eighteenth: V18,
 val nineteenth: V19)

data class Tuple20
<V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19, V20>
(val first: V1,
 val second: V2,
 val third: V3,
 val fourth: V4,
 val fifth: V5,
 val sixth: V6,
 val seventh: V7,
 val eighth: V8,
 val ninth: V9,
 val tenth: V10,
 val eleventh: V11,
 val twelfth: V12,
 val thirteenth: V13,
 val fourteenth: V14,
 val fifteenth: V15,
 val sixteenth: V16,
 val seventeenth: V17,
 val eighteenth: V18,
 val nineteenth: V19,
 val twentieth: V20)