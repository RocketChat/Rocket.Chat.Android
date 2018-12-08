package chat.rocket.android.userdetails.presentation

import chat.rocket.core.model.ChatRoom

interface UserDetailsView {

    fun showUserDetails(avatarUrl: String?, username: String?, name: String?, utcOffset: Float?, status: String, chatRoom: ChatRoom?)

    fun toDirectMessage(chatRoom: ChatRoom)

    fun onOpenDirectMessageError()
}
