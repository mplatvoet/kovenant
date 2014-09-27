package nl.mplatvoet.kotlin.komponents.promises

/**
 * Created by mplatvoet on 30-5-2014.
 */
private val NULL_VALUE: Any = Any()
private fun mask(value: Any?): Any = value ?: NULL_VALUE
private fun unmask(value: Any?): Any? = if (value == NULL_VALUE) null else value