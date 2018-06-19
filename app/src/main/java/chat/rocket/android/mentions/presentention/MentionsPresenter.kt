package chat.rocket.android.mentions.presentention

import chat.rocket.android.chatroom.uimodel.UiModelMapper
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.ChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.getMentions
import timber.log.Timber
import javax.inject.Inject

class MentionsPresenter @Inject constructor(
    private val view: MentionsView,
    private val strategy: CancelStrategy,
    private val roomsInteractor: ChatRoomsInteractor,
    private val mapper: UiModelMapper,
    val serverInteractor: GetCurrentServerInteractor,
    val factory: RocketChatClientFactory
) {
    private val serverUrl = serverInteractor.get()!!
    private val client = factory.create(serverUrl)
    private var offset: Long = 0

    /**
     * Loads all mentions for the given room id.
     *
     * @param roomId The id of the room to get the mentions from.
     */
    fun loadMentions(roomId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                roomsInteractor.getById(serverUrl, roomId)?.let {
                    val mentions = client.getMentions(roomId, it.type, offset, 30)
                    val mentionsList = mapper.map(mentions.result, asNotReversed = true)
                    view.showMentions(mentionsList)
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