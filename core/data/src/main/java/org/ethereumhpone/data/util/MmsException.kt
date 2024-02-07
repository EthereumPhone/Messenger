package org.ethereumhpone.data.util


open class MmsException : Exception {
    /**
     * Creates a new MmsException.
     */
    constructor() : super()

    /**
     * Creates a new MmsException with the specified detail message.
     *
     * @param message the detail message.
     */
    constructor(message: String?) : super(message)

    /**
     * Creates a new MmsException with the specified cause.
     *
     * @param cause the cause.
     */
    constructor(cause: Throwable?) : super(cause)

    /**
     * Creates a new MmsException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    companion object {
        private const val serialVersionUID = -7323249827281485390L
    }
}
