package org.ethereumhpone.data.util


class InvalidHeaderValueException : MmsException {
    /**
     * Constructs an InvalidHeaderValueException with no detailed message.
     */
    constructor() : super()

    /**
     * Constructs an InvalidHeaderValueException with the specified detailed message.
     *
     * @param message the detailed message.
     */
    constructor(message: String?) : super(message)

    companion object {
        private const val serialVersionUID = -2053384496042052262L
    }
}
