package chat.rocket.android.members.uimodel

import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.common.model.User
import chat.rocket.common.model.UserStatus
import chat.rocket.core.model.Value

class MemberUiModel(
    private val member: User,
    private val settings: Map<String, Value<Any>>,
    private val baseUrl: String?
) {
    val userId: String = member.id
    val avatarUri: String?
    val displayName: String
    val realName: String?
    val username: String?
    val email: String?
    val utcOffset: Float?
    val status: UserStatus?

    init {
        avatarUri = getUserAvatar()
        displayName = getUserDisplayName()
        realName = getUserRealName()
        username = getUserUsername()
        email = getUserEmail()
        utcOffset = getUserUtcOffset()
        status = getUserStatus()
    }

    private fun getUserAvatar(): String? {
        val username = member.username ?: "?"
        return baseUrl?.let {
            baseUrl.avatarUrl(username, format = "png")
        }
    }

    private fun getUserDisplayName(): String {
        val username = member.username
        val realName = member.name
        val senderName = if (settings.useRealName()) realName else username
        return senderName ?: username.toString()
    }

    private fun getUserRealName(): String? = member.name

    private fun getUserUsername(): String? = member.username

    private fun getUserEmail(): String? = member.emails?.get(0)?.address

    private fun getUserUtcOffset(): Float? = member.utcOffset

    private fun getUserStatus(): UserStatus? = member.status
}
