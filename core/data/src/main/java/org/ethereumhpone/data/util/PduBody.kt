package org.ethereumhpone.data.util

import java.util.Vector


class PduBody {
    private val mParts: Vector<PduPart>
    private val mPartMapByContentId: MutableMap<String, PduPart>
    private val mPartMapByContentLocation: MutableMap<String, PduPart>
    private val mPartMapByName: MutableMap<String, PduPart>
    private val mPartMapByFileName: MutableMap<String, PduPart>

    /**
     * Constructor.
     */
    init {
        mParts = Vector()
        mPartMapByContentId = HashMap()
        mPartMapByContentLocation = HashMap()
        mPartMapByName = HashMap()
        mPartMapByFileName = HashMap()
    }

    private fun putPartToMaps(part: PduPart) {
        // Put part to mPartMapByContentId.
        val contentId: ByteArray? = part.contentId
        if (null != contentId) {
            mPartMapByContentId[String(contentId)] = part
        }

        // Put part to mPartMapByContentLocation.
        val contentLocation: ByteArray? = part.contentLocation
        if (null != contentLocation) {
            val clc = String(contentLocation)
            mPartMapByContentLocation[clc] = part
        }

        // Put part to mPartMapByName.
        val name: ByteArray? = part.name
        if (null != name) {
            val clc = String(name)
            mPartMapByName[clc] = part
        }

        // Put part to mPartMapByFileName.
        val fileName: ByteArray? = part.filename
        if (null != fileName) {
            val clc = String(fileName)
            mPartMapByFileName[clc] = part
        }
    }

    /**
     * Appends the specified part to the end of this body.
     *
     * @param part part to be appended
     * @return true when success, false when fail
     * @throws NullPointerException when part is null
     */
    fun addPart(part: PduPart?): Boolean {
        if (null == part) {
            throw NullPointerException()
        }
        putPartToMaps(part)
        return mParts.add(part)
    }

    /**
     * Inserts the specified part at the specified position.
     *
     * @param index index at which the specified part is to be inserted
     * @param part part to be inserted
     * @throws NullPointerException when part is null
     */
    fun addPart(index: Int, part: PduPart?) {
        if (null == part) {
            throw NullPointerException()
        }
        putPartToMaps(part)
        mParts.add(index, part)
    }

    /**
     * Removes the part at the specified position.
     *
     * @param index index of the part to return
     * @return part at the specified index
     */
    fun removePart(index: Int): PduPart {
        return mParts.removeAt(index)
    }

    /**
     * Remove all of the parts.
     */
    fun removeAll() {
        mParts.clear()
    }

    /**
     * Get the part at the specified position.
     *
     * @param index index of the part to return
     * @return part at the specified index
     */
    fun getPart(index: Int): PduPart {
        return mParts[index]
    }

    /**
     * Get the index of the specified part.
     *
     * @param part the part object
     * @return index the index of the first occurrence of the part in this body
     */
    fun getPartIndex(part: PduPart): Int {
        return mParts.indexOf(part)
    }

    val partsNum: Int
        /**
         * Get the number of parts.
         *
         * @return the number of parts
         */
        get() = mParts.size

    /**
     * Get pdu part by content id.
     *
     * @param cid the value of content id.
     * @return the pdu part.
     */
    fun getPartByContentId(cid: String): PduPart? {
        return mPartMapByContentId[cid]
    }

    /**
     * Get pdu part by Content-Location. Content-Location of part is
     * the same as filename and name(param of content-type).
     *
     * @param contentLocation the value of filename.
     * @return the pdu part.
     */
    fun getPartByContentLocation(contentLocation: String): PduPart? {
        return mPartMapByContentLocation[contentLocation]
    }

    /**
     * Get pdu part by name.
     *
     * @param name the value of filename.
     * @return the pdu part.
     */
    fun getPartByName(name: String): PduPart? {
        return mPartMapByName[name]
    }

    /**
     * Get pdu part by filename.
     *
     * @param filename the value of filename.
     * @return the pdu part.
     */
    fun getPartByFileName(filename: String): PduPart? {
        return mPartMapByFileName[filename]
    }
}
