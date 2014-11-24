package nl.mplatvoet.kotlin.komponents.promises

/**
 * Created by mplatvoet on 30-5-2014.
 */
private trait ResultVisitor<V, E> {
    fun visitValue(value: V)
    fun visitError(error: E)
}

private trait Result<V, E> {
    fun accept(visitor: ResultVisitor<V, E>)
    val rawValue : Any
}
private class ValueResult<V, E>(val value: V) : Result<V, E> {
    override val rawValue: Any get() = value
    override fun accept(visitor: ResultVisitor<V, E>) {
        visitor.visitValue(value)
    }

}
private class ErrorResult<V, E>(val error: E) : Result<V, E> {
    override val rawValue: Any get() = error
    override fun accept(visitor: ResultVisitor<V, E>) {
        visitor.visitError(error)
    }
}