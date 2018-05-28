package chat.rocket.android.files.viewmodel

import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.baseUrl
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.GenericAttachment
import javax.inject.Inject

class FileViewModelMapper @Inject constructor(
    serverInteractor: GetCurrentServerInteractor,
    getSettingsInteractor: GetSettingsInteractor,
    private val tokenRepository: TokenRepository
) {
    private var settings: Map<String, Value<Any>> =
        getSettingsInteractor.get(serverInteractor.get()!!)
    private val baseUrl = settings.baseUrl()

    fun mapToViewModelList(fileList: List<GenericAttachment>): List<FileViewModel> {
        return fileList.map { FileViewModel(it, settings, tokenRepository, baseUrl) }
    }
}