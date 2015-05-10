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

package nl.mplatvoet.komponents.kovenant

//Can't use default values because type inference breaks
public fun all<V>(vararg promise: Promise<V, Exception>): Promise<List<V>, Exception> = concreteAll(Kovenant.context, true, *promise)

public fun all<V>(context: Context, vararg promise: Promise<V, Exception>): Promise<List<V>, Exception> = concreteAll(context, true, *promise)

//Can't use default values because type inference breaks
public fun any<V>(vararg promise: Promise<V, Exception>): Promise<V, List<Exception>> = concreteAny(Kovenant.context, true, *promise)

public fun any<V>(context: Context, vararg promise: Promise<V, Exception>): Promise<V, List<Exception>> = concreteAny(context, true, *promise)
