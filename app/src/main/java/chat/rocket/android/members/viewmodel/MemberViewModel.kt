package chat.rocket.android.members.viewmodel

import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.useRealName
import chat.rocket.common.model.User
import chat.rocket.core.model.Value

class MemberViewModel(private val member: User, private val settings: Map<String, Value<Any>>, private val baseUrl: String?) {
    val avatarUri: String?
    val memberName: CharSequence

    init {
        avatarUri = getUserAvatar()
        memberName = getUserName()
    }

    private fun getUserAvatar(): String? {
        val username = member.username ?: "?"
        return baseUrl?.let {
            UrlHelper.getAvatarUrl(baseUrl, username)
        }
    }

    private fun getUserName(): CharSequence {
        val username = member.username
        val realName = member.name
        val senderName = if (settings.useRealName()) realName else username
        return senderName ?: username.toString()
    }
}