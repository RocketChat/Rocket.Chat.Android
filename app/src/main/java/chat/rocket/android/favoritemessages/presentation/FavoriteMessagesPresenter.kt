package chat.rocket.android.favoritemessages.presentation

import chat.rocket.android.chatroom.uimodel.UiModelMapper
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.getFavoriteMessages
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class FavoriteMessagesPresenter @Inject constructor(
    private val view: FavoriteMessagesView,
    private val strategy: CancelStrategy,
    private val dbManager: DatabaseManager,
    @Named("currentServer") private val currentServer: String,
    private val mapper: UiModelMapper,
    val factory: RocketChatClientFactory
) {
    private val client: RocketChatClient = factory.get(currentServer)
    private var offset: Int = 0

    /**
     * Loads all favorite messages for the given room id.
     *
     * @param roomId The id of the room to get favorite messages from.
     */
    fun loadFavoriteMessages(roomId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                dbManager.getRoom(roomId)?.let {
                    val favoriteMessages = client.getFavoriteMessages(roomId, roomTypeOf(it.chatRoom.type), offset)
                    val messageList = mapper.map(favoriteMessages.result, asNotReversed = true)
                    view.showFavoriteMessages(messageList)
                    offset += 1 * 30
                }.ifNull {
                    Timber.e("Couldn't find a room with id: $roomId at current server.")
                }
            } catch (exception: RocketChatException) {
                Timber.e(exception)
            } finally {
                view.hideLoading()
            }
        }
    }
}