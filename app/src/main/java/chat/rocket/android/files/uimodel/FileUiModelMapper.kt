package chat.rocket.android.files.uimodel

import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.baseUrl
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.GenericAttachment
import javax.inject.Inject

class FileUiModelMapper @Inject constructor(
    serverInteractor: GetCurrentServerInteractor,
    getSettingsInteractor: GetSettingsInteractor,
    private val tokenRepository: TokenRepository
) {
    private var settings: Map<String, Value<Any>> =
        getSettingsInteractor.get(serverInteractor.get()!!)
    private val baseUrl = settings.baseUrl()

    fun mapToUiModelList(fileList: List<GenericAttachment>): List<FileUiModel> {
        return fileList.map { FileUiModel(it, settings, tokenRepository, baseUrl) }
    }
}