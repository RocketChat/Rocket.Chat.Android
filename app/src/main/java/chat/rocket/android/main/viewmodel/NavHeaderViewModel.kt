package chat.rocket.android.main.viewmodel

import chat.rocket.common.model.UserStatus


data class NavHeaderViewModel(
    val userDisplayName: String,
    val userStatus: UserStatus?,
    val userAvatar: String?,
    val serverUrl: String,
    val serverLogo: String?
)