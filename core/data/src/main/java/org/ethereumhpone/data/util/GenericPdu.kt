package org.ethereumhpone.data.util

import org.ethereumhpone.data.util.pdu_alt.PduHeaders


open class GenericPdu {
    /**
     * Get the headers of this PDU.
     *
     * @return A PduHeaders of this PDU.
     */
    /**
     * The headers of pdu.
     */
    var mPduHeaders: PduHeaders? = null

    /**
     * Constructor.
     */
    constructor() {
        mPduHeaders = PduHeaders()
    }

    /**
     * Constructor.
     *
     * @param headers Headers for this PDU.
     */
    internal constructor(headers: PduHeaders?) {
        mPduHeaders = headers
    }

    @set:Throws(InvalidHeaderValueException::class)
    var messageType: Int
        /**
         * Get X-Mms-Message-Type field value.
         *
         * @return the X-Mms-Report-Allowed value
         */
        get() = mPduHeaders!!.getOctet(PduHeaders.MESSAGE_TYPE)
        /**
         * Set X-Mms-Message-Type field value.
         *
         * @param value the value
         * @throws InvalidHeaderValueException if the value is invalid.
         * RuntimeException if field's value is not Octet.
         */
        set(value) {
            mPduHeaders!!.setOctet(value, PduHeaders.MESSAGE_TYPE)
        }

    @set:Throws(InvalidHeaderValueException::class)
    var mmsVersion: Int
        /**
         * Get X-Mms-MMS-Version field value.
         *
         * @return the X-Mms-MMS-Version value
         */
        get() = mPduHeaders!!.getOctet(PduHeaders.MMS_VERSION)
        /**
         * Set X-Mms-MMS-Version field value.
         *
         * @param value the value
         * @throws InvalidHeaderValueException if the value is invalid.
         * RuntimeException if field's value is not Octet.
         */
        set(value) {
            mPduHeaders!!.setOctet(value, PduHeaders.MMS_VERSION)
        }
    var from: EncodedStringValue?
        /**
         * Get From value.
         * From-value = Value-length
         * (Address-present-token Encoded-string-value | Insert-address-token)
         *
         * @return the value
         */
        get() = mPduHeaders!!.getEncodedStringValue(PduHeaders.FROM)
        /**
         * Set From value.
         *
         * @param value the value
         * @throws NullPointerException if the value is null.
         */
        set(value) {
            mPduHeaders!!.setEncodedStringValue(value, PduHeaders.FROM)
        }
}
