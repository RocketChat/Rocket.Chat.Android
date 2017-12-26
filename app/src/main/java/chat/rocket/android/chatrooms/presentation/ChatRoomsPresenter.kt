package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.util.launchUI
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.chatRooms
import javax.inject.Inject

class ChatRoomsPresenter @Inject constructor(private val view: ChatRoomsView, private val strategy: CancelStrategy) {
    @Inject lateinit var client: RocketChatClient

    fun getChatRooms() {
        launchUI(strategy) {
            view.showLoading()
            // TODO How to get the chat rooms?
            //view.showChatRooms(client.chatRooms().update.toMutableList())
            view.hideLoading()
        }
    }
}