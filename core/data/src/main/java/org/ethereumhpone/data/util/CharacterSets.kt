package org.ethereumhpone.data.util

import java.io.UnsupportedEncodingException
import java.util.HashMap

object CharacterSets {
    /**
     * IANA assigned MIB enum numbers.
     *
     * From wap-230-wsp-20010705-a.pdf
     * Any-charset = <Octet 128>
     * Equivalent to the special RFC2616 charset value "*"
     */
    const val ANY_CHARSET = 0x00
    const val US_ASCII = 0x03
    const val ISO_8859_1 = 0x04
    const val ISO_8859_2 = 0x05
    const val ISO_8859_3 = 0x06
    const val ISO_8859_4 = 0x07
    const val ISO_8859_5 = 0x08
    const val ISO_8859_6 = 0x09
    const val ISO_8859_7 = 0x0A
    const val ISO_8859_8 = 0x0B
    const val ISO_8859_9 = 0x0C
    const val SHIFT_JIS = 0x11
    const val UTF_8 = 0x6A
    const val BIG5 = 0x07EA
    const val UCS2 = 0x03E8
    const val UTF_16 = 0x03F7

    /**
     * If the encoding of given data is unsupported, use UTF_8 to decode it.
     */
    const val DEFAULT_CHARSET = UTF_8

    /**
     * Array of MIB enum numbers.
     */
    private val MIBENUM_NUMBERS = intArrayOf(
        ANY_CHARSET,
        US_ASCII,
        ISO_8859_1,
        ISO_8859_2,
        ISO_8859_3,
        ISO_8859_4,
        ISO_8859_5,
        ISO_8859_6,
        ISO_8859_7,
        ISO_8859_8,
        ISO_8859_9,
        SHIFT_JIS,
        UTF_8,
        BIG5,
        UCS2,
        UTF_16
    )

    /**
     * The Well-known-charset Mime name.
     */
    const val MIMENAME_ANY_CHARSET = "*"
    const val MIMENAME_US_ASCII = "us-ascii"
    const val MIMENAME_ISO_8859_1 = "iso-8859-1"
    const val MIMENAME_ISO_8859_2 = "iso-8859-2"
    const val MIMENAME_ISO_8859_3 = "iso-8859-3"
    const val MIMENAME_ISO_8859_4 = "iso-8859-4"
    const val MIMENAME_ISO_8859_5 = "iso-8859-5"
    const val MIMENAME_ISO_8859_6 = "iso-8859-6"
    const val MIMENAME_ISO_8859_7 = "iso-8859-7"
    const val MIMENAME_ISO_8859_8 = "iso-8859-8"
    const val MIMENAME_ISO_8859_9 = "iso-8859-9"
    const val MIMENAME_SHIFT_JIS = "shift_JIS"
    const val MIMENAME_UTF_8 = "utf-8"
    const val MIMENAME_BIG5 = "big5"
    const val MIMENAME_UCS2 = "iso-10646-ucs-2"
    const val MIMENAME_UTF_16 = "utf-16"

    const val DEFAULT_CHARSET_NAME = MIMENAME_UTF_8

    /**
     * Array of the names of character sets.
     */
    private val MIME_NAMES = arrayOf(
        MIMENAME_ANY_CHARSET,
        MIMENAME_US_ASCII,
        MIMENAME_ISO_8859_1,
        MIMENAME_ISO_8859_2,
        MIMENAME_ISO_8859_3,
        MIMENAME_ISO_8859_4,
        MIMENAME_ISO_8859_5,
        MIMENAME_ISO_8859_6,
        MIMENAME_ISO_8859_7,
        MIMENAME_ISO_8859_8,
        MIMENAME_ISO_8859_9,
        MIMENAME_SHIFT_JIS,
        MIMENAME_UTF_8,
        MIMENAME_BIG5,
        MIMENAME_UCS2,
        MIMENAME_UTF_16
    )

    private val MIBENUM_TO_NAME_MAP: MutableMap<Int, String>
    private val NAME_TO_MIBENUM_MAP: MutableMap<String, Int>

    init {
        // Create the HashMaps.
        MIBENUM_TO_NAME_MAP = HashMap()
        NAME_TO_MIBENUM_MAP = HashMap()
        assert(MIBENUM_NUMBERS.size == MIME_NAMES.size)
        val count = MIBENUM_NUMBERS.size - 1
        for (i in 0..count) {
            MIBENUM_TO_NAME_MAP[MIBENUM_NUMBERS[i]] = MIME_NAMES[i]
            NAME_TO_MIBENUM_MAP[MIME_NAMES[i]] = MIBENUM_NUMBERS[i]
        }
    }

    /**
     * Map an MIBEnum number to the name of the charset which this number
     * is assigned to by IANA.
     *
     * @param mibEnumValue An IANA assigned MIBEnum number.
     * @return The name string of the charset.
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getMimeName(mibEnumValue: Int): String {
        val name = MIBENUM_TO_NAME_MAP[mibEnumValue]
        requireNotNull(name) { "Unsupported encoding" }
        return name
    }

    /**
     * Map a well-known charset name to its assigned MIBEnum number.
     *
     * @param mimeName The charset name.
     * @return The MIBEnum number assigned by IANA for this charset.
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    fun getMibEnumValue(mimeName: String?): Int {
        if (mimeName == null) {
            return -1
        }
        val mibEnumValue = NAME_TO_MIBENUM_MAP[mimeName]
        requireNotNull(mibEnumValue) { "Unsupported encoding" }
        return mibEnumValue
    }
}