package chat.rocket.android.files.uimodel

import DateTimeHelper
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.util.extensions.fileUrl
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.GenericAttachment

class FileUiModel(
    private val genericAttachment: GenericAttachment,
    private val settings: Map<String, Value<Any>>,
    private val tokenRepository: TokenRepository,
    private val baseUrl: String
) {
    val name: String?
    val uploader: String?
    val uploadDate: String?
    val url: String?
    val isMedia: Boolean
    val isImage: Boolean

    init {
        name = getFileName()
        uploader = getUserDisplayName()
        uploadDate = getFileUploadDate()
        url = getFileUrl()
        isMedia = isFileMedia()
        isImage = isFileImage()
    }

    private fun getFileName(): String? {
        return genericAttachment.name
    }

    private fun getUserDisplayName(): String {
        val username = "@${genericAttachment.user.username}"
        val realName = genericAttachment.user.name
        val uploaderName = if (settings.useRealName()) realName else username
        return uploaderName ?: username
    }

    private fun getFileUploadDate(): String {
        genericAttachment.uploadedAt?.let {
            return DateTimeHelper.getDateTime(DateTimeHelper.getLocalDateTime(it))
        }
        return ""
    }

    private fun getFileUrl(): String? {
        val token = tokenRepository.get(baseUrl)
        if (token != null) {
            genericAttachment.path?.let {
                return baseUrl.fileUrl(it, token)
            }
        }
        return ""
    }

    private fun isFileMedia(): Boolean {
        genericAttachment.type?.let {
            return it.contains("audio") || it.contains("video")
        }
        return false
    }

    private fun isFileImage(): Boolean {
        genericAttachment.type?.let {
            return it.contains("image")
        }
        return false
    }
}