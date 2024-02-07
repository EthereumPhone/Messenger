package org.ethereumhpone.data.util

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class EncodedStringValue : Cloneable {
    /**
     * Get Char-set value.
     *
     * @return the value
     */// TODO: CharSet needs to be validated against MIBEnum.
    /**
     * Set Char-set value.
     *
     * @param charset the Char-set value
     */
    /**
     * The Char-set value.
     */
    var characterSet = 0

    /**
     * The Text-string value.
     */
    private var mData: ByteArray? = null

    /**
     * Constructor.
     *
     * @param charset the Char-set value
     * @param data the Text-string value
     * @throws NullPointerException if Text-string value is null.
     */
    constructor(charset: Int, data: ByteArray?) {
        // TODO: CharSet needs to be validated against MIBEnum.
        if (null == data) {
            throw NullPointerException("EncodedStringValue: Text-string is null.")
        }
        characterSet = charset
        mData = ByteArray(data.size)
        System.arraycopy(data, 0, mData, 0, data.size)
    }

    /**
     * Constructor.
     *
     * @param data the Text-string value
     * @throws NullPointerException if Text-string value is null.
     */
    constructor(data: ByteArray?) : this(CharacterSets.DEFAULT_CHARSET, data)
    constructor(data: String) {
        try {
            mData = data.toByteArray(Charset.forName(CharacterSets.DEFAULT_CHARSET_NAME))
            characterSet = CharacterSets.DEFAULT_CHARSET
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    var textString: ByteArray?
        /**
         * Get Text-string value.
         *
         * @return the value
         */
        get() {
            val byteArray = ByteArray(mData!!.size)
            System.arraycopy(mData, 0, byteArray, 0, mData!!.size)
            return byteArray
        }
        /**
         * Set Text-string value.
         *
         * @param textString the Text-string value
         * @throws NullPointerException if Text-string value is null.
         */
        set(textString) {
            if (null == textString) {
                throw NullPointerException("EncodedStringValue: Text-string is null.")
            }
            mData = ByteArray(textString.size)
            System.arraycopy(textString, 0, mData, 0, textString.size)
        }
    val string: String
        /**
         * Convert this object to a [String]. If the encoding of
         * the EncodedStringValue is null or unsupported, it will be
         * treated as iso-8859-1 encoding.
         *
         * @return The decoded String.
         */
        get() = if (CharacterSets.ANY_CHARSET === characterSet) {
            String(mData!!) // system default encoding.
        } else {
            try {
                val name: String = CharacterSets.getMimeName(characterSet)
                String(mData!!, charset(name))
            } catch (e: UnsupportedEncodingException) {
                if (LOCAL_LOGV) {
                    e.printStackTrace()
                }
                try {
                    String(mData!!, Charset.forName(CharacterSets.MIMENAME_ISO_8859_1))
                } catch (f: UnsupportedEncodingException) {
                    String(mData!!) // system default encoding.
                }
            }
        }

    /**
     * Append to Text-string.
     *
     * @param textString the textString to append
     * @throws NullPointerException if the text String is null
     * or an IOException occured.
     */
    fun appendTextString(textString: ByteArray?) {
        if (null == textString) {
            throw NullPointerException("Text-string is null.")
        }
        if (null == mData) {
            mData = ByteArray(textString.size)
            System.arraycopy(textString, 0, mData, 0, textString.size)
        } else {
            val newTextString = ByteArrayOutputStream()
            try {
                newTextString.write(mData)
                newTextString.write(textString)
            } catch (e: IOException) {
                e.printStackTrace()
                e.printStackTrace()
                throw NullPointerException(
                    "appendTextString: failed when write a new Text-string"
                )
            }
            mData = newTextString.toByteArray()
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        super.clone()
        val len = mData!!.size
        val dstBytes = ByteArray(len)
        System.arraycopy(mData, 0, dstBytes, 0, len)
        return try {
            EncodedStringValue(characterSet, dstBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            throw CloneNotSupportedException(e.message)
        }
    }

    /**
     * Split this encoded string around matches of the given pattern.
     *
     * @param pattern the delimiting pattern
     * @return the array of encoded strings computed by splitting this encoded
     * string around matches of the given pattern
     */
    fun split(pattern: String): Array<EncodedStringValue?>? {
        val temp = string.split(pattern.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val ret = arrayOfNulls<EncodedStringValue>(temp.size)
        for (i in ret.indices) {
            try {
                ret[i] = EncodedStringValue(
                    characterSet,
                    temp[i].toByteArray()
                )
            } catch (e: NullPointerException) {
                // Can't arrive here
                return null
            }
        }
        return ret
    }

    companion object {
        private const val LOCAL_LOGV = false

        /**
         * Extract an EncodedStringValue[] from a given String.
         */
        fun extract(src: String): Array<EncodedStringValue>? {
            val values = src.split(";".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val list = ArrayList<EncodedStringValue>()
            for (i in values.indices) {
                if (values[i].length > 0) {
                    list.add(EncodedStringValue(values[i]))
                }
            }
            val len = list.size
            return if (len > 0) {
                list.toTypedArray()
            } else {
                null
            }
        }

        /**
         * Concatenate an EncodedStringValue[] into a single String.
         */
        fun concat(addr: Array<EncodedStringValue>): String {
            val sb = StringBuilder()
            val maxIndex = addr.size - 1
            for (i in 0..maxIndex) {
                sb.append(addr[i].string)
                if (i < maxIndex) {
                    sb.append(";")
                }
            }
            return sb.toString()
        }

        fun copy(value: EncodedStringValue?): EncodedStringValue? {
            return if (value == null) {
                null
            } else EncodedStringValue(value.characterSet, value.mData)
        }

        fun encodeStrings(array: Array<String>): Array<EncodedStringValue?>? {
            val count = array.size
            if (count > 0) {
                val encodedArray = arrayOfNulls<EncodedStringValue>(count)
                for (i in 0 until count) {
                    encodedArray[i] = EncodedStringValue(array[i])
                }
                return encodedArray
            }
            return null
        }
    }
}
