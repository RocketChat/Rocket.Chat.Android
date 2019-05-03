package chat.rocket.android.server.domain.model

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Account(
    val serverName: String?,
    val serverUrl: String,
    val serverLogo: String?,
    val serverBg: String?,
    val userName: String,
    val avatar: String?
)