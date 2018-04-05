package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.ViewModelMapper
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.*
import chat.rocket.core.model.Message
import chat.rocket.core.model.Value
import chat.rocket.core.model.isSystemMessage
import timber.log.Timber
import javax.inject.Inject

class PinnedMessagesPresenter @Inject constructor(private val view: PinnedMessagesView,
                                                  private val strategy: CancelStrategy,
                                                  private val serverInteractor: GetCurrentServerInteractor,
                                                  private val roomsInteractor: GetChatRoomsInteractor,
                                                  private val mapper: ViewModelMapper,
                                                  factory: RocketChatClientFactory,
                                                  getSettingsInteractor: GetSettingsInteractor,
                                                  private val messagesRepository: MessagesRepository,
                                                  private val permissions: GetPermissionsInteractor) {

    private val client = factory.create(serverInteractor.get()!!)
    private var settings: Map<String, Value<Any>> = getSettingsInteractor.get(serverInteractor.get()!!)
    private var pinnedMessagesListOffset: Int = 0

    /**
     * Load all pinned messages for the given room id.
     *
     * @param roomId The id of the room to get pinned messages from.
     */
    fun loadPinnedMessages(roomId: String) {
        launchUI(strategy) {
            try {
                val serverUrl = serverInteractor.get()!!
                val chatRoom = roomsInteractor.getById(serverUrl, roomId)
                chatRoom?.let { room ->
                    view.showLoading()
                    val pinnedMessages =
                        client.getRoomPinnedMessages(roomId, room.type, pinnedMessagesListOffset)
                    pinnedMessagesListOffset = pinnedMessages.offset.toInt()
                    val messageList = mapper.map(pinnedMessages.result.filterNot { it.isSystemMessage() })
                    view.showPinnedMessages(messageList)
                    view.hideLoading()
                }.ifNull {
                    Timber.e("Couldn't find a room with id: $roomId at current server.")
                }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun unpinMessage(messageId: String) {
        launchUI(strategy) {
            if (!permissions.allowedMessagePinning()) {
                view.showMessage(R.string.permission_pinning_not_allowed)
                return@launchUI
            }
            try {
                client.unpinMessage(messageId)
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun deleteMessage(roomId: String, id: String){
        launchUI(strategy) {
            if (!permissions.allowedMessageDeleting()) {
                return@launchUI
            }
            //TODO: Default delete message always to true. Until we have the permissions system
            //implemented, a user will only be able to delete his own messages.
            try {
                client.deleteMessage(roomId, id, true)
                // if Message_ShowDeletedStatus == true an update to that message will be dispatched.
                // Otherwise we signalize that we just want the message removed.
                if (!permissions.showDeletedStatus()) {
                    view.dispatchDeleteMessage(id)
                }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun copyMessage(messageId: String) {
        launchUI(strategy) {
            try {
                messagesRepository.getById(messageId)?.let { m ->
                    view.copyToClipboard(m.message)
                }
            } catch (e: RocketChatException) {
                Timber.e(e)
            }
        }
    }

    fun showReactions(messageId: String) {
        view.showReactionsPopup(messageId)
    }

    /**
     * Send an emoji reaction to a message.
     */
    fun react(messageId: String, emoji: String) {
        launchUI(strategy) {
            try {
                client.toggleReaction(messageId, emoji.removeSurrounding(":"))
                Timber.e("emoji react")
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    /**
     * Update message identified by given id with given text.
     *
     * @param roomId The id of the room of the message.
     * @param messageId The id of the message to update.
     * @param text The updated text.
     */
    fun editMessage(roomId: String, messageId: String, text: String) {
        launchUI(strategy) {
            if (!permissions.allowedMessageEditing()) {
                view.showMessage(R.string.permission_editing_not_allowed)
                return@launchUI
            }
            view.showEditingAction(roomId, messageId, text)
        }
    }


    /**
     * Quote or reply a message.
     *
     * @param roomType The current room type.
     * @param roomName The name of the current room.
     * @param messageId The id of the message to make citation for.
     * @param mentionAuthor true means the citation is a reply otherwise it's a quote.
     */
    fun citeMessage(roomType: String, roomName: String, messageId: String, mentionAuthor: Boolean) {
        launchUI(strategy) {
            val message = messagesRepository.getById(messageId)
            val me = client.me() //TODO: Cache this and use an interactor
            val serverUrl = serverInteractor.get()!!
            message?.let { m ->
                val id = m.id
                val username = m.sender?.username
                val user = "@" + if (settings.useRealName()) m.sender?.name
                        ?: m.sender?.username else m.sender?.username
                val mention = if (mentionAuthor && me.username != username) user else ""
                val type = roomTypeOf(roomType)
                val room = when (type) {
                    is RoomType.Channel -> "channel"
                    is RoomType.DirectMessage -> "direct"
                    is RoomType.PrivateGroup -> "group"
                    is RoomType.Livechat -> "livechat"
                    is RoomType.Custom -> "custom" //TODO: put appropriate callback string here.
                }
                view.showReplyingAction(
                        username = user,
                        replyMarkdown = "[ ]($serverUrl/$room/$roomName?msg=$id) $mention ",
                        quotedMessage = m.message
                )
            }
        }
    }

}