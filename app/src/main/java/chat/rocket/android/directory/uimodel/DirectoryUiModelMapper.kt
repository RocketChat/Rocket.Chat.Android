package chat.rocket.android.directory.uimodel

import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.baseUrl
import chat.rocket.core.model.DirectoryResult
import chat.rocket.core.model.Value
import javax.inject.Inject
import javax.inject.Named

class DirectoryUiModelMapper @Inject constructor(
    getSettingsInteractor: GetSettingsInteractor,
    @Named("currentServer") private val currentServer: String,
    tokenRepository: TokenRepository
) {
    private var settings: Map<String, Value<Any>> = getSettingsInteractor.get(currentServer)
    private val baseUrl = settings.baseUrl()
    private val token = tokenRepository.get(currentServer)

    fun mapToUiModelList(directoryList: List<DirectoryResult>): List<DirectoryUiModel> {
        return directoryList.map { DirectoryUiModel(it, baseUrl, token) }
    }
}