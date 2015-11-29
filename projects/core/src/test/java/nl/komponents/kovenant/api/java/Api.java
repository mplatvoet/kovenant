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

package nl.komponents.kovenant.api.java;


import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import nl.komponents.kovenant.Promise;

import static nl.komponents.kovenant.KovenantApi.async;
import static nl.komponents.kovenant.KovenantApi.then;

/*
Indicator file for interoperability with Java. So no real test case, just
 */
public class Api {
    public static void main(String[] args) {
        Promise<Integer, Exception> promise = async(new Function0<Integer>() {
            @Override
            public Integer invoke() {
                return 1;
            }
        });

        Promise<Integer, Exception> next = then(promise, new Function1<Integer, Integer>() {
            @Override
            public Integer invoke(Integer integer) {
                return integer + 3;
            }
        });

        next.success(new Function1<Integer, Unit>() {
            @Override
            public Unit invoke(Integer integer) {
                System.out.println("integer = " + integer);
                return Unit.INSTANCE;
            }
        });


    }
}
