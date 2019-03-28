package chat.rocket.android.chatdetails.presentation

import chat.rocket.android.chatdetails.domain.ChatDetails
import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.favorite
import chat.rocket.core.internal.rest.getInfo
import chat.rocket.core.model.Room
import timber.log.Timber
import javax.inject.Inject

class ChatDetailsPresenter @Inject constructor(
    private val view: ChatDetailsView,
    private val navigator: ChatRoomNavigator,
    private val strategy: CancelStrategy,
    serverInteractor: GetCurrentServerInteractor,
    factory: ConnectionManagerFactory
) {
    private val currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client

    fun toggleFavoriteChatRoom(roomId: String, isFavorite: Boolean) {
        launchUI(strategy) {
            try {
                // Note: If it is favorite then the user wants to remove the favorite - and vice versa.
                retryIO("favorite($roomId, ${!isFavorite})") {
                    client.favorite(roomId, !isFavorite)
                }
                view.showFavoriteIcon(!isFavorite)
            } catch (e: RocketChatException) {
                Timber.e(
                    e,
                    "Error while trying to favorite or removing the favorite of a chat room."
                )
                e.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun toVideoConference(roomId: String, chatRoomType: String) =
        navigator.toVideoConference(roomId, chatRoomType)

    fun getDetails(chatRoomId: String, chatRoomType: String) {
        launchUI(strategy) {
            try {
                val room = retryIO("getInfo($chatRoomId, null, $chatRoomType") {
                    client.getInfo(chatRoomId, null, roomTypeOf(chatRoomType))
                }
                view.displayDetails(roomToChatDetails(room))
            } catch (exception: Exception) {
                Timber.e(exception)
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun toFiles(chatRoomId: String) {
        navigator.toFileList(chatRoomId)
    }

    fun toMembers(chatRoomId: String) {
        navigator.toMembersList(chatRoomId)
    }

    fun toMentions(chatRoomId: String) {
        navigator.toMentions(chatRoomId)
    }

    fun toPinned(chatRoomId: String) {
        navigator.toPinnedMessageList(chatRoomId)
    }

    fun toFavorites(chatRoomId: String) {
        navigator.toFavoriteMessageList(chatRoomId)
    }

    private fun roomToChatDetails(room: Room): ChatDetails {
        return with(room) {
            ChatDetails(
                name = name,
                fullName = fullName,
                type = type.toString(),
                topic = topic,
                description = description,
                announcement = announcement
            )
        }
    }
}