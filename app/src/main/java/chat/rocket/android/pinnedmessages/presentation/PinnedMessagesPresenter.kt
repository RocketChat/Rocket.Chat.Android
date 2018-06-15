package chat.rocket.android.pinnedmessages.presentation

import chat.rocket.android.chatroom.uimodel.UiModelMapper
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.ChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.getPinnedMessages
import timber.log.Timber
import javax.inject.Inject

class PinnedMessagesPresenter @Inject constructor(
    private val view: PinnedMessagesView,
    private val strategy: CancelStrategy,
    private val roomsInteractor: ChatRoomsInteractor,
    private val mapper: UiModelMapper,
    val serverInteractor: GetCurrentServerInteractor,
    val factory: RocketChatClientFactory
) {
    private val serverUrl = serverInteractor.get()!!
    private val client = factory.create(serverUrl)
    private var offset: Int = 0

    /**
     * Loads all pinned messages for the given room id.
     *
     * @param roomId The id of the room to get pinned messages from.
     */
    fun loadPinnedMessages(roomId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                roomsInteractor.getById(serverUrl, roomId)?.let {
                    val pinnedMessages = client.getPinnedMessages(roomId, it.type, offset)
                    val messageList = mapper.map(pinnedMessages.result, asNotReversed = true)
                    view.showPinnedMessages(messageList)
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