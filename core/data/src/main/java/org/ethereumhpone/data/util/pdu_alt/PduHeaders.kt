package org.ethereumhpone.data.util.pdu_alt

import org.ethereumhpone.data.util.EncodedStringValue
import org.ethereumhpone.data.util.InvalidHeaderValueException


class PduHeaders {
    /**
     * The map contains the value of all headers.
     */
    private var mHeaderMap: HashMap<Int, Any?>? = null

    /**
     * Constructor of PduHeaders.
     */
    init {
        mHeaderMap = HashMap()
    }

    /**
     * Get octet value by header field.
     *
     * @param field the field
     * @return the octet value of the pdu header
     * with specified header field. Return 0 if
     * the value is not set.
     */
    fun getOctet(field: Int): Int {
        return mHeaderMap!![field] as Int ?: return 0
    }

    /**
     * Set octet value to pdu header by header field.
     *
     * @param value the value
     * @param field the field
     * @throws InvalidHeaderValueException if the value is invalid.
     */
    fun setOctet(value: Int, field: Int) {
        /**
         * Check whether this field can be set for specific
         * header and check validity of the field.
         */
        var value = value
        when (field) {
            REPORT_ALLOWED, ADAPTATION_ALLOWED, DELIVERY_REPORT, DRM_CONTENT, DISTRIBUTION_INDICATOR, QUOTAS, READ_REPORT, STORE, STORED, TOTALS, SENDER_VISIBILITY -> if (VALUE_YES != value && VALUE_NO != value) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            READ_STATUS -> if (READ_STATUS_READ != value && READ_STATUS__DELETED_WITHOUT_BEING_READ != value) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            CANCEL_STATUS -> if (CANCEL_STATUS_REQUEST_SUCCESSFULLY_RECEIVED != value && CANCEL_STATUS_REQUEST_CORRUPTED != value) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            PRIORITY -> if (value < PRIORITY_LOW || value > PRIORITY_HIGH) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            STATUS -> if (value < STATUS_EXPIRED || value > STATUS_UNREACHABLE) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            REPLY_CHARGING -> if (value < REPLY_CHARGING_REQUESTED || value > REPLY_CHARGING_ACCEPTED_TEXT_ONLY) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            MM_STATE -> if (value < MM_STATE_DRAFT || value > MM_STATE_FORWARDED) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            RECOMMENDED_RETRIEVAL_MODE -> if (RECOMMENDED_RETRIEVAL_MODE_MANUAL != value) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            CONTENT_CLASS -> if (value < CONTENT_CLASS_TEXT || value > CONTENT_CLASS_CONTENT_RICH) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            RETRIEVE_STATUS ->                 // According to oma-ts-mms-enc-v1_3, section 7.3.50, we modify the invalid value.
                if (value > RETRIEVE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM && value < RETRIEVE_STATUS_ERROR_PERMANENT_FAILURE) {
                    value = RETRIEVE_STATUS_ERROR_TRANSIENT_FAILURE
                } else if (value > RETRIEVE_STATUS_ERROR_PERMANENT_CONTENT_UNSUPPORTED && value <= RETRIEVE_STATUS_ERROR_END) {
                    value = RETRIEVE_STATUS_ERROR_PERMANENT_FAILURE
                } else if (value < RETRIEVE_STATUS_OK || value > RETRIEVE_STATUS_OK && value < RETRIEVE_STATUS_ERROR_TRANSIENT_FAILURE || value > RETRIEVE_STATUS_ERROR_END) {
                    value = RETRIEVE_STATUS_ERROR_PERMANENT_FAILURE
                }

            STORE_STATUS ->                 // According to oma-ts-mms-enc-v1_3, section 7.3.58, we modify the invalid value.
                if (value > STORE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM && value < STORE_STATUS_ERROR_PERMANENT_FAILURE) {
                    value = STORE_STATUS_ERROR_TRANSIENT_FAILURE
                } else if (value > STORE_STATUS_ERROR_PERMANENT_MMBOX_FULL && value <= STORE_STATUS_ERROR_END) {
                    value = STORE_STATUS_ERROR_PERMANENT_FAILURE
                } else if (value < STORE_STATUS_SUCCESS || value > STORE_STATUS_SUCCESS && value < STORE_STATUS_ERROR_TRANSIENT_FAILURE || value > STORE_STATUS_ERROR_END) {
                    value = STORE_STATUS_ERROR_PERMANENT_FAILURE
                }

            RESPONSE_STATUS ->                 // According to oma-ts-mms-enc-v1_3, section 7.3.48, we modify the invalid value.
                if (value > RESPONSE_STATUS_ERROR_TRANSIENT_PARTIAL_SUCCESS && value < RESPONSE_STATUS_ERROR_PERMANENT_FAILURE) {
                    value = RESPONSE_STATUS_ERROR_TRANSIENT_FAILURE
                } else if (value > RESPONSE_STATUS_ERROR_PERMANENT_LACK_OF_PREPAID && value <= RESPONSE_STATUS_ERROR_PERMANENT_END || value < RESPONSE_STATUS_OK || value > RESPONSE_STATUS_ERROR_UNSUPPORTED_MESSAGE && value < RESPONSE_STATUS_ERROR_TRANSIENT_FAILURE || value > RESPONSE_STATUS_ERROR_PERMANENT_END) {
                    value = RESPONSE_STATUS_ERROR_PERMANENT_FAILURE
                }

            MMS_VERSION -> if (value < MMS_VERSION_1_0 || value > MMS_VERSION_1_3) {
                value = CURRENT_MMS_VERSION // Current version is the default value.
            }

            MESSAGE_TYPE -> if (value < MESSAGE_TYPE_SEND_REQ || value > MESSAGE_TYPE_CANCEL_CONF) {
                // Invalid value.
                throw InvalidHeaderValueException("Invalid Octet value!")
            }

            else ->                 // This header value should not be Octect.
                throw RuntimeException("Invalid header field!")
        }
        mHeaderMap!![field] = value
    }

    /**
     * Get TextString value by header field.
     *
     * @param field the field
     * @return the TextString value of the pdu header
     * with specified header field
     */
    fun getTextString(field: Int): ByteArray? {
        return mHeaderMap!![field] as ByteArray?
    }

    /**
     * Set TextString value to pdu header by header field.
     *
     * @param value the value
     * @param field the field
     * @return the TextString value of the pdu header
     * with specified header field
     * @throws NullPointerException if the value is null.
     */
    fun setTextString(value: ByteArray?, field: Int) {
        /**
         * Check whether this field can be set for specific
         * header and check validity of the field.
         */
        if (null == value) {
            throw NullPointerException()
        }
        when (field) {
            TRANSACTION_ID, REPLY_CHARGING_ID, AUX_APPLIC_ID, APPLIC_ID, REPLY_APPLIC_ID, MESSAGE_ID, REPLACE_ID, CANCEL_ID, CONTENT_LOCATION, MESSAGE_CLASS, CONTENT_TYPE -> {}
            else ->                 // This header value should not be Text-String.
                throw RuntimeException("Invalid header field!")
        }
        mHeaderMap!![field] = value
    }

    /**
     * Get EncodedStringValue value by header field.
     *
     * @param field the field
     * @return the EncodedStringValue value of the pdu header
     * with specified header field
     */
    fun getEncodedStringValue(field: Int): EncodedStringValue? {
        return mHeaderMap!![field] as EncodedStringValue?
    }

    /**
     * Get TO, CC or BCC header value.
     *
     * @param field the field
     * @return the EncodeStringValue array of the pdu header
     * with specified header field
     */
    fun getEncodedStringValues(field: Int): Array<EncodedStringValue>? {
        val list = mHeaderMap?.get(field) as ArrayList<EncodedStringValue>? ?: return null
        val values = arrayOfNulls<EncodedStringValue>(list.size)
        return list.toArray(values)
    }

    /**
     * Set EncodedStringValue value to pdu header by header field.
     *
     * @param value the value
     * @param field the field
     * @return the EncodedStringValue value of the pdu header
     * with specified header field
     * @throws NullPointerException if the value is null.
     */
    fun setEncodedStringValue(value: EncodedStringValue?, field: Int) {
        /**
         * Check whether this field can be set for specific
         * header and check validity of the field.
         */
        if (null == value) {
            throw NullPointerException()
        }
        when (field) {
            SUBJECT, RECOMMENDED_RETRIEVAL_MODE_TEXT, RETRIEVE_TEXT, STATUS_TEXT, STORE_STATUS_TEXT, RESPONSE_TEXT, FROM, PREVIOUSLY_SENT_BY, MM_FLAGS -> {}
            else ->                 // This header value should not be Encoded-String-Value.
                throw RuntimeException("Invalid header field!")
        }
        mHeaderMap!![field] = value
    }

    /**
     * Set TO, CC or BCC header value.
     *
     * @param value the value
     * @param field the field
     * @return the EncodedStringValue value array of the pdu header
     * with specified header field
     * @throws NullPointerException if the value is null.
     */
    protected fun setEncodedStringValues(value: Array<EncodedStringValue>?, field: Int) {
        /**
         * Check whether this field can be set for specific
         * header and check validity of the field.
         */
        if (null == value) {
            throw NullPointerException()
        }
        when (field) {
            BCC, CC, TO -> {}
            else ->                 // This header value should not be Encoded-String-Value.
                throw RuntimeException("Invalid header field!")
        }
        val list: ArrayList<EncodedStringValue> = ArrayList<EncodedStringValue>()
        for (i in value.indices) {
            list.add(value[i])
        }
        mHeaderMap!![field] = list
    }

    /**
     * Append one EncodedStringValue to another.
     *
     * @param value the EncodedStringValue to append
     * @param field the field
     * @throws NullPointerException if the value is null.
     */
    fun appendEncodedStringValue(
        value: EncodedStringValue?,
        field: Int
    ) {
        if (null == value) {
            throw NullPointerException()
        }
        when (field) {
            BCC, CC, TO -> {}
            else -> throw RuntimeException("Invalid header field!")
        }
        var list: ArrayList<EncodedStringValue?>? =
            mHeaderMap!![field] as ArrayList<EncodedStringValue?>?
        if (null == list) {
            list = ArrayList<EncodedStringValue?>()
        }
        list!!.add(value)
        mHeaderMap!![field] = list
    }

    /**
     * Get LongInteger value by header field.
     *
     * @param field the field
     * @return the LongInteger value of the pdu header
     * with specified header field. if return -1, the
     * field is not existed in pdu header.
     */
    fun getLongInteger(field: Int): Long {
        return mHeaderMap!![field] as Long ?: return -1
    }

    /**
     * Set LongInteger value to pdu header by header field.
     *
     * @param value the value
     * @param field the field
     */
    fun setLongInteger(value: Long, field: Int) {
        /**
         * Check whether this field can be set for specific
         * header and check validity of the field.
         */
        when (field) {
            DATE, REPLY_CHARGING_SIZE, MESSAGE_SIZE, MESSAGE_COUNT, START, LIMIT, DELIVERY_TIME, EXPIRY, REPLY_CHARGING_DEADLINE, PREVIOUSLY_SENT_DATE -> {}
            else ->                 // This header value should not be LongInteger.
                throw RuntimeException("Invalid header field!")
        }
        mHeaderMap!![field] = value
    }

    companion object {
        /**
         * All pdu header fields.
         */
        const val BCC = 0x81
        const val CC = 0x82
        const val CONTENT_LOCATION = 0x83
        const val CONTENT_TYPE = 0x84
        const val DATE = 0x85
        const val DELIVERY_REPORT = 0x86
        const val DELIVERY_TIME = 0x87
        const val EXPIRY = 0x88
        const val FROM = 0x89
        const val MESSAGE_CLASS = 0x8A
        const val MESSAGE_ID = 0x8B
        const val MESSAGE_TYPE = 0x8C
        const val MMS_VERSION = 0x8D
        const val MESSAGE_SIZE = 0x8E
        const val PRIORITY = 0x8F
        const val READ_REPLY = 0x90
        const val READ_REPORT = 0x90
        const val REPORT_ALLOWED = 0x91
        const val RESPONSE_STATUS = 0x92
        const val RESPONSE_TEXT = 0x93
        const val SENDER_VISIBILITY = 0x94
        const val STATUS = 0x95
        const val SUBJECT = 0x96
        const val TO = 0x97
        const val TRANSACTION_ID = 0x98
        const val RETRIEVE_STATUS = 0x99
        const val RETRIEVE_TEXT = 0x9A
        const val READ_STATUS = 0x9B
        const val REPLY_CHARGING = 0x9C
        const val REPLY_CHARGING_DEADLINE = 0x9D
        const val REPLY_CHARGING_ID = 0x9E
        const val REPLY_CHARGING_SIZE = 0x9F
        const val PREVIOUSLY_SENT_BY = 0xA0
        const val PREVIOUSLY_SENT_DATE = 0xA1
        const val STORE = 0xA2
        const val MM_STATE = 0xA3
        const val MM_FLAGS = 0xA4
        const val STORE_STATUS = 0xA5
        const val STORE_STATUS_TEXT = 0xA6
        const val STORED = 0xA7
        const val ATTRIBUTES = 0xA8
        const val TOTALS = 0xA9
        const val MBOX_TOTALS = 0xAA
        const val QUOTAS = 0xAB
        const val MBOX_QUOTAS = 0xAC
        const val MESSAGE_COUNT = 0xAD
        const val CONTENT = 0xAE
        const val START = 0xAF
        const val ADDITIONAL_HEADERS = 0xB0
        const val DISTRIBUTION_INDICATOR = 0xB1
        const val ELEMENT_DESCRIPTOR = 0xB2
        const val LIMIT = 0xB3
        const val RECOMMENDED_RETRIEVAL_MODE = 0xB4
        const val RECOMMENDED_RETRIEVAL_MODE_TEXT = 0xB5
        const val STATUS_TEXT = 0xB6
        const val APPLIC_ID = 0xB7
        const val REPLY_APPLIC_ID = 0xB8
        const val AUX_APPLIC_ID = 0xB9
        const val CONTENT_CLASS = 0xBA
        const val DRM_CONTENT = 0xBB
        const val ADAPTATION_ALLOWED = 0xBC
        const val REPLACE_ID = 0xBD
        const val CANCEL_ID = 0xBE
        const val CANCEL_STATUS = 0xBF

        /**
         * X-Mms-Message-Type field types.
         */
        const val MESSAGE_TYPE_SEND_REQ = 0x80
        const val MESSAGE_TYPE_SEND_CONF = 0x81
        const val MESSAGE_TYPE_NOTIFICATION_IND = 0x82
        const val MESSAGE_TYPE_NOTIFYRESP_IND = 0x83
        const val MESSAGE_TYPE_RETRIEVE_CONF = 0x84
        const val MESSAGE_TYPE_ACKNOWLEDGE_IND = 0x85
        const val MESSAGE_TYPE_DELIVERY_IND = 0x86
        const val MESSAGE_TYPE_READ_REC_IND = 0x87
        const val MESSAGE_TYPE_READ_ORIG_IND = 0x88
        const val MESSAGE_TYPE_FORWARD_REQ = 0x89
        const val MESSAGE_TYPE_FORWARD_CONF = 0x8A
        const val MESSAGE_TYPE_MBOX_STORE_REQ = 0x8B
        const val MESSAGE_TYPE_MBOX_STORE_CONF = 0x8C
        const val MESSAGE_TYPE_MBOX_VIEW_REQ = 0x8D
        const val MESSAGE_TYPE_MBOX_VIEW_CONF = 0x8E
        const val MESSAGE_TYPE_MBOX_UPLOAD_REQ = 0x8F
        const val MESSAGE_TYPE_MBOX_UPLOAD_CONF = 0x90
        const val MESSAGE_TYPE_MBOX_DELETE_REQ = 0x91
        const val MESSAGE_TYPE_MBOX_DELETE_CONF = 0x92
        const val MESSAGE_TYPE_MBOX_DESCR = 0x93
        const val MESSAGE_TYPE_DELETE_REQ = 0x94
        const val MESSAGE_TYPE_DELETE_CONF = 0x95
        const val MESSAGE_TYPE_CANCEL_REQ = 0x96
        const val MESSAGE_TYPE_CANCEL_CONF = 0x97

        /**
         * X-Mms-Delivery-Report |
         * X-Mms-Read-Report |
         * X-Mms-Report-Allowed |
         * X-Mms-Sender-Visibility |
         * X-Mms-Store |
         * X-Mms-Stored |
         * X-Mms-Totals |
         * X-Mms-Quotas |
         * X-Mms-Distribution-Indicator |
         * X-Mms-DRM-Content |
         * X-Mms-Adaptation-Allowed |
         * field types.
         */
        const val VALUE_YES = 0x80
        const val VALUE_NO = 0x81

        /**
         * Delivery-Time |
         * Expiry and Reply-Charging-Deadline |
         * field type components.
         */
        const val VALUE_ABSOLUTE_TOKEN = 0x80
        const val VALUE_RELATIVE_TOKEN = 0x81

        /**
         * X-Mms-MMS-Version field types.
         */
        const val MMS_VERSION_1_3 = 1 shl 4 or 3
        const val MMS_VERSION_1_2 = 1 shl 4 or 2
        const val MMS_VERSION_1_1 = 1 shl 4 or 1
        const val MMS_VERSION_1_0 = 1 shl 4 or 0

        // Current version is 1.2.
        const val CURRENT_MMS_VERSION = MMS_VERSION_1_2

        /**
         * From field type components.
         */
        const val FROM_ADDRESS_PRESENT_TOKEN = 0x80
        const val FROM_INSERT_ADDRESS_TOKEN = 0x81
        const val FROM_ADDRESS_PRESENT_TOKEN_STR = "address-present-token"
        const val FROM_INSERT_ADDRESS_TOKEN_STR = "insert-address-token"

        /**
         * X-Mms-Status Field.
         */
        const val STATUS_EXPIRED = 0x80
        const val STATUS_RETRIEVED = 0x81
        const val STATUS_REJECTED = 0x82
        const val STATUS_DEFERRED = 0x83
        const val STATUS_UNRECOGNIZED = 0x84
        const val STATUS_INDETERMINATE = 0x85
        const val STATUS_FORWARDED = 0x86
        const val STATUS_UNREACHABLE = 0x87

        /**
         * MM-Flags field type components.
         */
        const val MM_FLAGS_ADD_TOKEN = 0x80
        const val MM_FLAGS_REMOVE_TOKEN = 0x81
        const val MM_FLAGS_FILTER_TOKEN = 0x82

        /**
         * X-Mms-Message-Class field types.
         */
        const val MESSAGE_CLASS_PERSONAL = 0x80
        const val MESSAGE_CLASS_ADVERTISEMENT = 0x81
        const val MESSAGE_CLASS_INFORMATIONAL = 0x82
        const val MESSAGE_CLASS_AUTO = 0x83
        const val MESSAGE_CLASS_PERSONAL_STR = "personal"
        const val MESSAGE_CLASS_ADVERTISEMENT_STR = "advertisement"
        const val MESSAGE_CLASS_INFORMATIONAL_STR = "informational"
        const val MESSAGE_CLASS_AUTO_STR = "auto"

        /**
         * X-Mms-Priority field types.
         */
        const val PRIORITY_LOW = 0x80
        const val PRIORITY_NORMAL = 0x81
        const val PRIORITY_HIGH = 0x82

        /**
         * X-Mms-Response-Status field types.
         */
        const val RESPONSE_STATUS_OK = 0x80
        const val RESPONSE_STATUS_ERROR_UNSPECIFIED = 0x81
        const val RESPONSE_STATUS_ERROR_SERVICE_DENIED = 0x82
        const val RESPONSE_STATUS_ERROR_MESSAGE_FORMAT_CORRUPT = 0x83
        const val RESPONSE_STATUS_ERROR_SENDING_ADDRESS_UNRESOLVED = 0x84
        const val RESPONSE_STATUS_ERROR_MESSAGE_NOT_FOUND = 0x85
        const val RESPONSE_STATUS_ERROR_NETWORK_PROBLEM = 0x86
        const val RESPONSE_STATUS_ERROR_CONTENT_NOT_ACCEPTED = 0x87
        const val RESPONSE_STATUS_ERROR_UNSUPPORTED_MESSAGE = 0x88
        const val RESPONSE_STATUS_ERROR_TRANSIENT_FAILURE = 0xC0
        const val RESPONSE_STATUS_ERROR_TRANSIENT_SENDNG_ADDRESS_UNRESOLVED = 0xC1
        const val RESPONSE_STATUS_ERROR_TRANSIENT_MESSAGE_NOT_FOUND = 0xC2
        const val RESPONSE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM = 0xC3
        const val RESPONSE_STATUS_ERROR_TRANSIENT_PARTIAL_SUCCESS = 0xC4
        const val RESPONSE_STATUS_ERROR_PERMANENT_FAILURE = 0xE0
        const val RESPONSE_STATUS_ERROR_PERMANENT_SERVICE_DENIED = 0xE1
        const val RESPONSE_STATUS_ERROR_PERMANENT_MESSAGE_FORMAT_CORRUPT = 0xE2
        const val RESPONSE_STATUS_ERROR_PERMANENT_SENDING_ADDRESS_UNRESOLVED = 0xE3
        const val RESPONSE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND = 0xE4
        const val RESPONSE_STATUS_ERROR_PERMANENT_CONTENT_NOT_ACCEPTED = 0xE5
        const val RESPONSE_STATUS_ERROR_PERMANENT_REPLY_CHARGING_LIMITATIONS_NOT_MET = 0xE6
        const val RESPONSE_STATUS_ERROR_PERMANENT_REPLY_CHARGING_REQUEST_NOT_ACCEPTED = 0xE6
        const val RESPONSE_STATUS_ERROR_PERMANENT_REPLY_CHARGING_FORWARDING_DENIED = 0xE8
        const val RESPONSE_STATUS_ERROR_PERMANENT_REPLY_CHARGING_NOT_SUPPORTED = 0xE9
        const val RESPONSE_STATUS_ERROR_PERMANENT_ADDRESS_HIDING_NOT_SUPPORTED = 0xEA
        const val RESPONSE_STATUS_ERROR_PERMANENT_LACK_OF_PREPAID = 0xEB
        const val RESPONSE_STATUS_ERROR_PERMANENT_END = 0xFF

        /**
         * X-Mms-Retrieve-Status field types.
         */
        const val RETRIEVE_STATUS_OK = 0x80
        const val RETRIEVE_STATUS_ERROR_TRANSIENT_FAILURE = 0xC0
        const val RETRIEVE_STATUS_ERROR_TRANSIENT_MESSAGE_NOT_FOUND = 0xC1
        const val RETRIEVE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM = 0xC2
        const val RETRIEVE_STATUS_ERROR_PERMANENT_FAILURE = 0xE0
        const val RETRIEVE_STATUS_ERROR_PERMANENT_SERVICE_DENIED = 0xE1
        const val RETRIEVE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND = 0xE2
        const val RETRIEVE_STATUS_ERROR_PERMANENT_CONTENT_UNSUPPORTED = 0xE3
        const val RETRIEVE_STATUS_ERROR_END = 0xFF

        /**
         * X-Mms-Sender-Visibility field types.
         */
        const val SENDER_VISIBILITY_HIDE = 0x80
        const val SENDER_VISIBILITY_SHOW = 0x81

        /**
         * X-Mms-Read-Status field types.
         */
        const val READ_STATUS_READ = 0x80
        const val READ_STATUS__DELETED_WITHOUT_BEING_READ = 0x81

        /**
         * X-Mms-Cancel-Status field types.
         */
        const val CANCEL_STATUS_REQUEST_SUCCESSFULLY_RECEIVED = 0x80
        const val CANCEL_STATUS_REQUEST_CORRUPTED = 0x81

        /**
         * X-Mms-Reply-Charging field types.
         */
        const val REPLY_CHARGING_REQUESTED = 0x80
        const val REPLY_CHARGING_REQUESTED_TEXT_ONLY = 0x81
        const val REPLY_CHARGING_ACCEPTED = 0x82
        const val REPLY_CHARGING_ACCEPTED_TEXT_ONLY = 0x83

        /**
         * X-Mms-MM-State field types.
         */
        const val MM_STATE_DRAFT = 0x80
        const val MM_STATE_SENT = 0x81
        const val MM_STATE_NEW = 0x82
        const val MM_STATE_RETRIEVED = 0x83
        const val MM_STATE_FORWARDED = 0x84

        /**
         * X-Mms-Recommended-Retrieval-Mode field types.
         */
        const val RECOMMENDED_RETRIEVAL_MODE_MANUAL = 0x80

        /**
         * X-Mms-Content-Class field types.
         */
        const val CONTENT_CLASS_TEXT = 0x80
        const val CONTENT_CLASS_IMAGE_BASIC = 0x81
        const val CONTENT_CLASS_IMAGE_RICH = 0x82
        const val CONTENT_CLASS_VIDEO_BASIC = 0x83
        const val CONTENT_CLASS_VIDEO_RICH = 0x84
        const val CONTENT_CLASS_MEGAPIXEL = 0x85
        const val CONTENT_CLASS_CONTENT_BASIC = 0x86
        const val CONTENT_CLASS_CONTENT_RICH = 0x87

        /**
         * X-Mms-Store-Status field types.
         */
        const val STORE_STATUS_SUCCESS = 0x80
        const val STORE_STATUS_ERROR_TRANSIENT_FAILURE = 0xC0
        const val STORE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM = 0xC1
        const val STORE_STATUS_ERROR_PERMANENT_FAILURE = 0xE0
        const val STORE_STATUS_ERROR_PERMANENT_SERVICE_DENIED = 0xE1
        const val STORE_STATUS_ERROR_PERMANENT_MESSAGE_FORMAT_CORRUPT = 0xE2
        const val STORE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND = 0xE3
        const val STORE_STATUS_ERROR_PERMANENT_MMBOX_FULL = 0xE4
        const val STORE_STATUS_ERROR_END = 0xFF
    }
}
