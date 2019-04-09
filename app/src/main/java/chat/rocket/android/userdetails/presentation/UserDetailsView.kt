package chat.rocket.android.userdetails.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface UserDetailsView : LoadingView, MessageView {

    /**
     * Shows user detail.
     *
     * @param avatarUrl The user avatar URL.
     * @param name The user's name.
     * @param username The user's username.
     * @param status The user's status.
     * @param utcOffset The user's UTC offset.
     * @param isVideoCallAllowed True if the video call is allowed, false otherwise.
     */
    fun showUserDetailsAndActions(
        avatarUrl: String,
        name: String,
        username: String,
        status: String,
        utcOffset: String,
        isVideoCallAllowed: Boolean
    )
}
