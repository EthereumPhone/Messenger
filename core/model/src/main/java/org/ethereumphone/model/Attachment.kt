package org.ethereumphone.model

import java.net.URI

data class Attachment(
    val id: String,
    val messageId: String,
    val type: AttachmentType,
    val uri: URI
)



enum class AttachmentType {
    IMAGE,
    VIDEO,
    GIF,
    AUDIO
}
