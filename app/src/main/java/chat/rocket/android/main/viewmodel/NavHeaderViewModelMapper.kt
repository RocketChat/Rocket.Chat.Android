package chat.rocket.android.main.viewmodel

import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.baseUrl
import chat.rocket.core.model.Myself
import chat.rocket.core.model.Value
import javax.inject.Inject

class NavHeaderViewModelMapper @Inject constructor(serverInteractor: GetCurrentServerInteractor, getSettingsInteractor: GetSettingsInteractor) {
    private var settings: Map<String, Value<Any>> = getSettingsInteractor.get(serverInteractor.get()!!)
    private val baseUrl = settings.baseUrl()

    fun mapToViewModel(me: Myself): NavHeaderViewModel {
        return NavHeaderViewModel(me, settings, baseUrl)
    }
}