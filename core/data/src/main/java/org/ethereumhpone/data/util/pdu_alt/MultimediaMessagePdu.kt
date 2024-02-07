package org.ethereumhpone.data.util.pdu_alt

import org.ethereumhpone.data.util.EncodedStringValue
import org.ethereumhpone.data.util.GenericPdu
import org.ethereumhpone.data.util.InvalidHeaderValueException
import org.ethereumhpone.data.util.PduBody


class MultimediaMessagePdu : GenericPdu {
    /**
     * Get body of the PDU.
     *
     * @return the body
     */
    /**
     * Set body of the PDU.
     *
     * @param body the body
     */
    /**
     * The body.
     */
    var body: PduBody? = null

    /**
     * Constructor.
     */
    constructor() : super()

    /**
     * Constructor.
     *
     * @param header the header of this PDU
     * @param body the body of this PDU
     */
    constructor(header: PduHeaders?, body: PduBody?) : super(header) {
        this.body = body
    }

    /**
     * Constructor with given headers.
     *
     * @param headers Headers for this PDU.
     */
    internal constructor(headers: PduHeaders?) : super(headers)

    var subject: EncodedStringValue?
        /**
         * Get subject.
         *
         * @return the value
         */
        get() = mPduHeaders?.getEncodedStringValue(PduHeaders.SUBJECT)
        /**
         * Set subject.
         *
         * @param value the value
         * @throws NullPointerException if the value is null.
         */
        set(value) {
            mPduHeaders?.setEncodedStringValue(value, PduHeaders.SUBJECT)
        }
    val to: Array<EncodedStringValue>?
        /**
         * Get To value.
         *
         * @return the value
         */
        get() = mPduHeaders?.getEncodedStringValues(PduHeaders.TO)

    /**
     * Add a "To" value.
     *
     * @param value the value
     * @throws NullPointerException if the value is null.
     */
    fun addTo(value: EncodedStringValue?) {
        mPduHeaders?.appendEncodedStringValue(value, PduHeaders.TO)
    }

    @set:Throws(InvalidHeaderValueException::class)
    var priority: Int?
        /**
         * Get X-Mms-Priority value.
         *
         * @return the value
         */
        get() = mPduHeaders?.getOctet(PduHeaders.PRIORITY)
        /**
         * Set X-Mms-Priority value.
         *
         * @param value the value
         * @throws InvalidHeaderValueException if the value is invalid.
         */
        set(value) {
            if (value != null) {
                mPduHeaders?.setOctet(value, PduHeaders.PRIORITY)
            }
        }
    var date: Long?
        /**
         * Get Date value.
         *
         * @return the value
         */
        get() = mPduHeaders?.getLongInteger(PduHeaders.DATE)
        /**
         * Set Date value in seconds.
         *
         * @param value the value
         */
        set(value) {
            if (value != null) {
                mPduHeaders?.setLongInteger(value, PduHeaders.DATE)
            }
        }
}
