package chat.rocket.android.chatroom.presentation

import javax.inject.Inject


class ChatRoomPresenter @Inject constructor(
    private val view: ChatRoomView,
    private val navigator: ChatRoomNavigator
) {
}