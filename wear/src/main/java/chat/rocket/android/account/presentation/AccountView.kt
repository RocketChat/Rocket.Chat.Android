package chat.rocket.android.account.presentation

import chat.rocket.android.core.behaviour.LoadingView
import chat.rocket.android.core.behaviour.MessagesView
import chat.rocket.common.model.UserStatus

interface AccountView : MessagesView, LoadingView {

    /**
     * Show User Profile
     *
     * @param realName Full name of the user
     * @param userName Username
     * @param avatarUrl User avatar URL
     * @param status The status of the user (either of busy, away, online, offline)
     */
    fun showProfile(
        realName: String,
        userName: String,
        avatarUrl: String,
        status: UserStatus?
    )
}