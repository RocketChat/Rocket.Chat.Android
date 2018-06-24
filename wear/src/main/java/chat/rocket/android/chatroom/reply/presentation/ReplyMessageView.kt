package chat.rocket.android.chatroom.reply.presentation

import chat.rocket.android.core.behaviour.LoadingView
import chat.rocket.android.core.behaviour.MessagesView

interface ReplyMessageView : MessagesView, LoadingView {

    fun messageSentSuccessfully()
}