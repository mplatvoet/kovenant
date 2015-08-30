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
package nl.komponents.kovenant.android

import nl.komponents.kovenant.Context
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.ui.alwaysUi as aui
import nl.komponents.kovenant.ui.failUi as fui
import nl.komponents.kovenant.ui.successUi as sui

public fun <V> promiseOnUi(context: Context = Kovenant.context,
                           alwaysSchedule: Boolean = false,
                           body: () -> V): Promise<V, Exception> {
    return nl.komponents.kovenant.ui.promiseOnUi(
            context = context,
            alwaysSchedule = alwaysSchedule,
            body = body)
}

//TODO figure out how to properly deprecate this with decent replacement
@deprecated("now part of kovenant-ui package", ReplaceWith("successUi(body)", "nl.komponents.kovenant.ui.successUi"))
public fun <V, E> Promise<V, E>.successUi(body: (value: V) -> Unit): Promise<V, E> = successUi(false, body)

public fun <V, E> Promise<V, E>.successUi(alwaysSchedule: Boolean, body: (value: V) -> Unit): Promise<V, E> {
    return this.sui(alwaysSchedule = alwaysSchedule, body = body)
}


public fun <V, E> Promise<V, E>.failUi(body: (error: E) -> Unit): Promise<V, E> = failUi(false, body)

public fun <V, E> Promise<V, E>.failUi(alwaysSchedule: Boolean, body: (error: E) -> Unit): Promise<V, E> {
    return this.fui(alwaysSchedule = alwaysSchedule, body = body)
}


public fun <V, E> Promise<V, E>.alwaysUi(body: () -> Unit): Promise<V, E> = alwaysUi(false, body)

public fun <V, E> Promise<V, E>.alwaysUi(alwaysSchedule: Boolean, body: () -> Unit): Promise<V, E> {
    return this.aui(alwaysSchedule = alwaysSchedule, body = body)
}