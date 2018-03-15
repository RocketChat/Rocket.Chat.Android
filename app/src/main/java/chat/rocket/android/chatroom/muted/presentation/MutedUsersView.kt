package chat.rocket.android.chatroom.muted.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface MutedUsersView: LoadingView, MessageView {
    fun showMutedUsers()
}
