package chat.rocket.android.members.uimodel

import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.baseUrl
import chat.rocket.common.model.User
import chat.rocket.core.model.Value
import javax.inject.Inject
import javax.inject.Named

class MemberUiModelMapper @Inject constructor(
    serverInteractor: GetCurrentServerInteractor,
    getSettingsInteractor: GetSettingsInteractor,
    @Named("currentServer") private val currentServer: String,
    tokenRepository: TokenRepository
) {
    private var settings: Map<String, Value<Any>> = getSettingsInteractor.get(serverInteractor.get()!!)
    private val baseUrl = settings.baseUrl()
    private val token = tokenRepository.get(currentServer)

    fun mapToUiModelList(memberList: List<User>): List<MemberUiModel> {
        return memberList.map { MemberUiModel(it, settings, baseUrl, token) }
    }
}
