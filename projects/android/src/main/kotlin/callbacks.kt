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


/*
 * Note, deprecation replacements don't seem to properly work under kotlin plugin version 0.12.1335, which is
 * the latest version at time of writing. Therefor disabled replacement. The easiest way to migrate is just doing a
 * search and replace on package names.
 */

import nl.komponents.kovenant.Context
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.ui.alwaysUi as newAlwaysUi
import nl.komponents.kovenant.ui.failUi as newFailUi
import nl.komponents.kovenant.ui.promiseOnUi as newPromiseOnUi
import nl.komponents.kovenant.ui.successUi as newSuccessUi


@deprecated("now part of kovenant-ui package, replace imports with 'nl.komponents.kovenant.ui'"
        /*, ReplaceWith("promiseOnUi(context, alwaysSchedule, body)", "nl.komponents.kovenant.ui.promiseOnUi")*/)
public fun <V> promiseOnUi(context: Context = Kovenant.context,
                           alwaysSchedule: Boolean = false,
                           body: () -> V): Promise<V, Exception> {
    return newPromiseOnUi(
            context = context,
            alwaysSchedule = alwaysSchedule,
            body = body)
}


@deprecated("now part of kovenant-ui package, replace imports with 'nl.komponents.kovenant.ui'"
        /*, ReplaceWith("successUi(body)", "nl.komponents.kovenant.ui.successUi")*/)
public fun <V, E> Promise<V, E>.successUi(body: (value: V) -> Unit): Promise<V, E> = newSuccessUi(body)

@deprecated("now part of kovenant-ui package, replace imports with 'nl.komponents.kovenant.ui'"
        /*, ReplaceWith("successUi(alwaysSchedule = alwaysSchedule, body = body)", "nl.komponents.kovenant.ui.successUi")*/)
public fun <V, E> Promise<V, E>.successUi(alwaysSchedule: Boolean, body: (value: V) -> Unit): Promise<V, E> {
    return newSuccessUi(alwaysSchedule = alwaysSchedule, body = body)
}

@deprecated("now part of kovenant-ui package, replace imports with 'nl.komponents.kovenant.ui'"
        /*, ReplaceWith("failUi(alwaysSchedule = false, body = body)", "nl.komponents.kovenant.ui.failUi")*/)
public fun <V, E> Promise<V, E>.failUi(body: (error: E) -> Unit): Promise<V, E> = newFailUi(alwaysSchedule = false, body = body)

@deprecated("now part of kovenant-ui package, replace imports with 'nl.komponents.kovenant.ui'"
        /*, ReplaceWith("failUi(alwaysSchedule = alwaysSchedule, body = body)", "nl.komponents.kovenant.ui.failUi")*/)
public fun <V, E> Promise<V, E>.failUi(alwaysSchedule: Boolean, body: (error: E) -> Unit): Promise<V, E> {
    return newFailUi(alwaysSchedule = alwaysSchedule, body = body)
}

@deprecated("now part of kovenant-ui package, replace imports with 'nl.komponents.kovenant.ui'"
        /*, ReplaceWith("alwaysUi(alwaysSchedule = false, body = body)", "nl.komponents.kovenant.ui.alwaysUi")*/)
public fun <V, E> Promise<V, E>.alwaysUi(body: () -> Unit): Promise<V, E> = newAlwaysUi(alwaysSchedule = false, body = body)

@deprecated("now part of kovenant-ui package, replace imports with 'nl.komponents.kovenant.ui'"
        /*, ReplaceWith("alwaysUi(alwaysSchedule = alwaysSchedule, body = body)", "nl.komponents.kovenant.ui.alwaysUi")*/)
public fun <V, E> Promise<V, E>.alwaysUi(alwaysSchedule: Boolean, body: () -> Unit): Promise<V, E> {
    return newAlwaysUi(alwaysSchedule = alwaysSchedule, body = body)
}