package chat.rocket.android.main.uimodel

import chat.rocket.common.model.UserStatus

data class NavHeaderUiModel(
    val userDisplayName: String?,
    val userStatus: UserStatus?,
    val userAvatar: String?,
    val serverUrl: String,
    val serverLogo: String?
)