package chat.rocket.android.server.domain.model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Account(
    val serverName: String?,
    val serverUrl: String,
    val serverLogoUrl: String?,
    val serverBackgroundImageUrl: String?,
    val userName: String,
    val userAvatarUrl: String?,
    val authToken: String?,
    val userId: String?
)