package chat.rocket.android.main.viewmodel

import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.*
import chat.rocket.core.model.Myself
import javax.inject.Inject

class NavHeaderViewModelMapper @Inject constructor(serverInteractor: GetCurrentServerInteractor,
                                                   getSettingsInteractor: GetSettingsInteractor) {
    private var settings: PublicSettings = getSettingsInteractor.get(serverInteractor.get()!!)
    private val baseUrl = settings.baseUrl()!!

    fun mapToViewModel(me: Myself): NavHeaderViewModel {
        val username = mapUsername(me)
        val thumb = me.username?.let { UrlHelper.getAvatarUrl(baseUrl, it) }
        val image = settings.wideTile() ?: settings.faviconLarge()
        val logo = image?.let { UrlHelper.getServerLogoUrl(baseUrl, it) }

        return NavHeaderViewModel(username, baseUrl, thumb, logo)
    }

    private fun mapUsername(me: Myself): String {
        val username = me.username
        val realName = me.name
        val senderName = if (settings.useRealName()) realName else username
        return senderName ?: username.toString()
    }
}