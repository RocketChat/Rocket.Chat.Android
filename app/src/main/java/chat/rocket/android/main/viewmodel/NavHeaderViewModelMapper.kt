package chat.rocket.android.main.viewmodel

import chat.rocket.android.server.domain.*
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.serverLogoUrl
import chat.rocket.core.model.Myself
import javax.inject.Inject

class NavHeaderViewModelMapper @Inject constructor(serverInteractor: GetCurrentServerInteractor,
                                                   getSettingsInteractor: GetSettingsInteractor) {
    private val currentServer = serverInteractor.get()!!
    private var settings: PublicSettings = getSettingsInteractor.get(currentServer)

    fun mapToViewModel(me: Myself): NavHeaderViewModel {
        val username = mapUsername(me)
        val thumb = me.username?.let { currentServer.avatarUrl(it) }
        val image = settings.wideTile() ?: settings.faviconLarge()
        val logo = image?.let { currentServer.serverLogoUrl(it) }

        return NavHeaderViewModel(username, currentServer, thumb, logo)
    }

    private fun mapUsername(me: Myself): String {
        val username = me.username
        val realName = me.name
        val senderName = if (settings.useRealName()) realName else username
        return senderName ?: username.toString()
    }
}