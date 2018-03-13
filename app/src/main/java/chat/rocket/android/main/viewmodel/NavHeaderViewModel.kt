package chat.rocket.android.main.viewmodel

import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.favicon
import chat.rocket.android.server.domain.useRealName
import chat.rocket.core.model.Myself
import chat.rocket.core.model.Value

class NavHeaderViewModel(private val me: Myself, private val settings: Map<String, Value<Any>>, private val baseUrl: String?) {
    val serverLogoUri: String?
    val userAvatarUri: String?
    val userDisplayName: String

    init {
        serverLogoUri = getServerLogo()
        userAvatarUri = getUserAvatar()
        userDisplayName = getUserName()
    }

    private fun getServerLogo(): String? {
        return baseUrl?.let {
            UrlHelper.getServerLogoUrl(it, settings.favicon())
        }
    }

    private fun getUserAvatar(): String? {
        val username = me.username ?: "?"
        return baseUrl?.let {
            UrlHelper.getAvatarUrl(baseUrl, username)
        }
    }

    private fun getUserName(): String {
        val username = me.username
        val realName = me.name
        val senderName = if (settings.useRealName()) realName else username
        return senderName ?: username.toString()
    }

}