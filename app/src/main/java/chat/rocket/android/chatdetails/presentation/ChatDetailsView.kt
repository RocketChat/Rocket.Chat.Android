package chat.rocket.android.chatdetails.presentation

import chat.rocket.android.chatdetails.domain.ChatDetails
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ChatDetailsView: MessageView {
    fun displayDetails(room: ChatDetails)
}