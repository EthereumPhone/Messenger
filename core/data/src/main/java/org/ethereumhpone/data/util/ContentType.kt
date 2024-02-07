package org.ethereumhpone.data.util


object ContentType {
    const val MMS_MESSAGE = "application/vnd.wap.mms-message"

    // The phony content type for generic PDUs (e.g. ReadOrig.ind,
    // Notification.ind, Delivery.ind).
    const val MMS_GENERIC = "application/vnd.wap.mms-generic"
    const val MULTIPART_MIXED = "application/vnd.wap.multipart.mixed"
    const val MULTIPART_RELATED = "application/vnd.wap.multipart.related"
    const val MULTIPART_ALTERNATIVE = "application/vnd.wap.multipart.alternative"
    const val MULTIPART_SIGNED = "multipart/signed"
    const val TEXT_PLAIN = "text/plain"
    const val TEXT_HTML = "text/html"
    const val TEXT_VCALENDAR = "text/x-vCalendar"
    const val TEXT_VCARD = "text/x-vCard"
    const val IMAGE_UNSPECIFIED = "image/*"
    const val IMAGE_JPEG = "image/jpeg"
    const val IMAGE_JPG = "image/jpg"
    const val IMAGE_GIF = "image/gif"
    const val IMAGE_WBMP = "image/vnd.wap.wbmp"
    const val IMAGE_PNG = "image/png"
    const val IMAGE_X_MS_BMP = "image/x-ms-bmp"
    const val AUDIO_UNSPECIFIED = "audio/*"
    const val AUDIO_AAC = "audio/aac"
    const val AUDIO_AAC_MP4 = "audio/aac_mp4"
    const val AUDIO_QCELP = "audio/qcelp"
    const val AUDIO_EVRC = "audio/evrc"
    const val AUDIO_AMR = "audio/amr"
    const val AUDIO_IMELODY = "audio/imelody"
    const val AUDIO_MID = "audio/mid"
    const val AUDIO_MIDI = "audio/midi"
    const val AUDIO_MP3 = "audio/mp3"
    const val AUDIO_MPEG3 = "audio/mpeg3"
    const val AUDIO_MPEG = "audio/mpeg"
    const val AUDIO_MPG = "audio/mpg"
    const val AUDIO_MP4 = "audio/mp4"
    const val AUDIO_X_MID = "audio/x-mid"
    const val AUDIO_X_MIDI = "audio/x-midi"
    const val AUDIO_X_MP3 = "audio/x-mp3"
    const val AUDIO_X_MPEG3 = "audio/x-mpeg3"
    const val AUDIO_X_MPEG = "audio/x-mpeg"
    const val AUDIO_X_MPG = "audio/x-mpg"
    const val AUDIO_3GPP = "audio/3gpp"
    const val AUDIO_X_WAV = "audio/x-wav"
    const val AUDIO_OGG = "application/ogg"
    const val VIDEO_UNSPECIFIED = "video/*"
    const val VIDEO_3GPP = "video/3gpp"
    const val VIDEO_3G2 = "video/3gpp2"
    const val VIDEO_H263 = "video/h263"
    const val VIDEO_MP4 = "video/mp4"
    const val APP_SMIL = "application/smil"
    const val APP_WAP_XHTML = "application/vnd.wap.xhtml+xml"
    const val APP_XHTML = "application/xhtml+xml"
    const val APP_DRM_CONTENT = "application/vnd.oma.drm.content"
    const val APP_DRM_MESSAGE = "application/vnd.oma.drm.message"
    private val sSupportedContentTypes = ArrayList<String>()
    private val sSupportedImageTypes = ArrayList<String>()
    private val sSupportedAudioTypes = ArrayList<String>()
    private val sSupportedVideoTypes = ArrayList<String>()

    init {
        sSupportedContentTypes.add(TEXT_PLAIN)
        sSupportedContentTypes.add(TEXT_HTML)
        sSupportedContentTypes.add(TEXT_VCALENDAR)
        sSupportedContentTypes.add(TEXT_VCARD)
        sSupportedContentTypes.add(IMAGE_JPEG)
        sSupportedContentTypes.add(IMAGE_GIF)
        sSupportedContentTypes.add(IMAGE_WBMP)
        sSupportedContentTypes.add(IMAGE_PNG)
        sSupportedContentTypes.add(IMAGE_JPG)
        sSupportedContentTypes.add(IMAGE_X_MS_BMP)
        //supportedContentTypes.add(IMAGE_SVG); not yet supported.
        sSupportedContentTypes.add(AUDIO_AAC)
        sSupportedContentTypes.add(AUDIO_AAC_MP4)
        sSupportedContentTypes.add(AUDIO_QCELP)
        sSupportedContentTypes.add(AUDIO_EVRC)
        sSupportedContentTypes.add(AUDIO_AMR)
        sSupportedContentTypes.add(AUDIO_IMELODY)
        sSupportedContentTypes.add(AUDIO_MID)
        sSupportedContentTypes.add(AUDIO_MIDI)
        sSupportedContentTypes.add(AUDIO_MP3)
        sSupportedContentTypes.add(AUDIO_MP4)
        sSupportedContentTypes.add(AUDIO_MPEG3)
        sSupportedContentTypes.add(AUDIO_MPEG)
        sSupportedContentTypes.add(AUDIO_MPG)
        sSupportedContentTypes.add(AUDIO_X_MID)
        sSupportedContentTypes.add(AUDIO_X_MIDI)
        sSupportedContentTypes.add(AUDIO_X_MP3)
        sSupportedContentTypes.add(AUDIO_X_MPEG3)
        sSupportedContentTypes.add(AUDIO_X_MPEG)
        sSupportedContentTypes.add(AUDIO_X_MPG)
        sSupportedContentTypes.add(AUDIO_X_WAV)
        sSupportedContentTypes.add(AUDIO_3GPP)
        sSupportedContentTypes.add(AUDIO_OGG)
        sSupportedContentTypes.add(VIDEO_3GPP)
        sSupportedContentTypes.add(VIDEO_3G2)
        sSupportedContentTypes.add(VIDEO_H263)
        sSupportedContentTypes.add(VIDEO_MP4)
        sSupportedContentTypes.add(APP_SMIL)
        sSupportedContentTypes.add(APP_WAP_XHTML)
        sSupportedContentTypes.add(APP_XHTML)
        sSupportedContentTypes.add(APP_DRM_CONTENT)
        sSupportedContentTypes.add(APP_DRM_MESSAGE)

        // add supported image types
        sSupportedImageTypes.add(IMAGE_JPEG)
        sSupportedImageTypes.add(IMAGE_GIF)
        sSupportedImageTypes.add(IMAGE_WBMP)
        sSupportedImageTypes.add(IMAGE_PNG)
        sSupportedImageTypes.add(IMAGE_JPG)
        sSupportedImageTypes.add(IMAGE_X_MS_BMP)

        // add supported audio types
        sSupportedAudioTypes.add(AUDIO_AAC)
        sSupportedAudioTypes.add(AUDIO_AAC_MP4)
        sSupportedAudioTypes.add(AUDIO_QCELP)
        sSupportedAudioTypes.add(AUDIO_EVRC)
        sSupportedAudioTypes.add(AUDIO_AMR)
        sSupportedAudioTypes.add(AUDIO_IMELODY)
        sSupportedAudioTypes.add(AUDIO_MID)
        sSupportedAudioTypes.add(AUDIO_MIDI)
        sSupportedAudioTypes.add(AUDIO_MP3)
        sSupportedAudioTypes.add(AUDIO_MPEG3)
        sSupportedAudioTypes.add(AUDIO_MPEG)
        sSupportedAudioTypes.add(AUDIO_MPG)
        sSupportedAudioTypes.add(AUDIO_MP4)
        sSupportedAudioTypes.add(AUDIO_X_MID)
        sSupportedAudioTypes.add(AUDIO_X_MIDI)
        sSupportedAudioTypes.add(AUDIO_X_MP3)
        sSupportedAudioTypes.add(AUDIO_X_MPEG3)
        sSupportedAudioTypes.add(AUDIO_X_MPEG)
        sSupportedAudioTypes.add(AUDIO_X_MPG)
        sSupportedAudioTypes.add(AUDIO_X_WAV)
        sSupportedAudioTypes.add(AUDIO_3GPP)
        sSupportedAudioTypes.add(AUDIO_OGG)

        // add supported video types
        sSupportedVideoTypes.add(VIDEO_3GPP)
        sSupportedVideoTypes.add(VIDEO_3G2)
        sSupportedVideoTypes.add(VIDEO_H263)
        sSupportedVideoTypes.add(VIDEO_MP4)
    }

    fun isSupportedType(contentType: String?): Boolean {
        return null != contentType && sSupportedContentTypes.contains(contentType)
    }

    fun isSupportedImageType(contentType: String?): Boolean {
        return isImageType(contentType) && isSupportedType(contentType)
    }

    fun isSupportedAudioType(contentType: String?): Boolean {
        return isAudioType(contentType) && isSupportedType(contentType)
    }

    fun isSupportedVideoType(contentType: String?): Boolean {
        return isVideoType(contentType) && isSupportedType(contentType)
    }

    fun isTextType(contentType: String?): Boolean {
        return null != contentType && contentType.startsWith("text/")
    }

    fun isImageType(contentType: String?): Boolean {
        return null != contentType && contentType.startsWith("image/")
    }

    fun isAudioType(contentType: String?): Boolean {
        return null != contentType && contentType.startsWith("audio/")
    }

    fun isVideoType(contentType: String?): Boolean {
        return null != contentType && contentType.startsWith("video/")
    }

    fun isDrmType(contentType: String?): Boolean {
        return (null != contentType
                && (contentType == APP_DRM_CONTENT || contentType == APP_DRM_MESSAGE))
    }

    fun isUnspecified(contentType: String?): Boolean {
        return null != contentType && contentType.endsWith("*")
    }

    val imageTypes: ArrayList<String>
        get() = sSupportedImageTypes.clone() as ArrayList<String>
    val audioTypes: ArrayList<String>
        get() = sSupportedAudioTypes.clone() as ArrayList<String>
    val videoTypes: ArrayList<String>
        get() = sSupportedVideoTypes.clone() as ArrayList<String>
    val supportedTypes: ArrayList<String>
        get() = sSupportedContentTypes.clone() as ArrayList<String>
}
