package nl.mplatvoet.kotlin.komponents.promises

/**
 * Created by mplatvoet on 30-5-2014.
 */
private trait ResultVisitor<T> {
    fun visitValue(value: T)
    fun visitException(e: Exception)
}

private trait Result<T> {
    fun accept(visitor: ResultVisitor<T>)
}
private class ValueResult<T>(val value: T) : Result<T> {
    override fun accept(visitor: ResultVisitor<T>) {
        visitor.visitValue(value)
    }

}
private class ExceptionResult<T>(val e: Exception) : Result<T> {
    override fun accept(visitor: ResultVisitor<T>) {
        visitor.visitException(e)
    }
}