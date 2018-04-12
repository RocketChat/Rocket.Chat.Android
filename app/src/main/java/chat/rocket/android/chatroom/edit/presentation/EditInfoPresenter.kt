package chat.rocket.android.chatroom.edit.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.*
import chat.rocket.core.model.ChatRoom
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class EditInfoPresenter @Inject constructor(private val view: EditInfoView,
                                            private val chatRoomsInteractor: GetChatRoomsInteractor,
                                            private val serverInteractor: GetCurrentServerInteractor,
                                            private val strategy: CancelStrategy,
                                            factory: RocketChatClientFactory) {
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)

    fun loadRoomInfo(chatRoomId: String) {
        launchUI(strategy) {
            view.showLoading()

            try {
                val chatRoom: ChatRoom = chatRoomsInteractor.getById(serverInteractor.get()!!, chatRoomId)!!
                view.showRoomInfo(chatRoom)
            } catch (ex: Exception) {
                ex.message.let {
                    view.showMessage(it!!)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun ifRoomUpdated() {
        launch(CommonPool + strategy.jobs) {
        }
    }

    fun saveChatInformation(chatRoomId: String, chatRoomType: String, name: String?,
                            topic: String?, announcement: String?, description: String?,
                            type: String, readOnly: Boolean, archived: Boolean,
                            currentRoomSettings: ChatRoom) {
        launchUI(strategy) {
            view.showLoading()

            val roomType = roomTypeOf(chatRoomType)

            try {
                if (name != currentRoomSettings.name)
                    client.rename(chatRoomId, roomType, name)
                if (topic != currentRoomSettings.topic)
                    client.setTopic(chatRoomId, roomType, topic)
                if (announcement != currentRoomSettings.announcement)
                   client.setAnnouncement(chatRoomId, roomType, announcement)
                if (description != currentRoomSettings.description)
                    client.setDescription(chatRoomId, roomType, description)
                if (readOnly != currentRoomSettings.readonly)
                    client.setReadOnly(chatRoomId, roomType, readOnly)
                if (archived != currentRoomSettings.archived)
                    client.archive(chatRoomId, roomType, archived)
                if (type != currentRoomSettings.type.toString())
                    client.setType(chatRoomId, roomType, type)
            } catch (ex: Exception) {
                ex.message.let {
                    view.showMessage(it!!)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }
}