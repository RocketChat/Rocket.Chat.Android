package chat.rocket.android.members.viewmodel

import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.baseUrl
import chat.rocket.common.model.User
import chat.rocket.core.model.Value
import javax.inject.Inject

class MemberViewModelMapper @Inject constructor(serverInteractor: GetCurrentServerInteractor, getSettingsInteractor: GetSettingsInteractor) {
    private var settings: Map<String, Value<Any>> = getSettingsInteractor.get(serverInteractor.get()!!)
    private val baseUrl = settings.baseUrl()

    fun mapToViewModelList(memberList: List<User>): List<MemberViewModel> {
        return memberList.map { MemberViewModel(it, settings, baseUrl) }
    }
}