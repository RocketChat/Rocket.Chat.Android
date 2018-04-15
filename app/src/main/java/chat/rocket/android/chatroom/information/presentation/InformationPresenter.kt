package chat.rocket.android.chatroom.information.presentation

import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
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
import chat.rocket.core.model.Room
import chat.rocket.core.model.Permission
import javax.inject.Inject

private const val EDIT_ROOM = "edit-room"

class InformationPresenter @Inject constructor(private val view: InformationView,
                                               private val chatRoomsInteractor: GetChatRoomsInteractor,
                                               private val serverInteractor: GetCurrentServerInteractor,
                                               private val navigator: ChatRoomNavigator,
                                               private val strategy: CancelStrategy,
                                               factory: RocketChatClientFactory) {
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)

    fun loadRoomInfo(chatRoomId: String, chatRoomType: String, isSubscribed: Boolean) {
        view.showLoading()

        launchUI(strategy) {
            try {
                if (isSubscribed) {
                    val chatRoom = chatRoomsInteractor.getById(serverInteractor.get()!!, chatRoomId)!!

                    val roomRoles: List<String>? = chatRoom.roles
                    val permissions: List<Permission> = client.permissions()

                    if (canEdit(roomRoles, permissions))
                        view.allowRoomEditing()

                    view.allowHideAndLeave(chatRoom.open)
                    view.showRoomInfo(chatRoom)
                }
                else {
                    val room = client.getInfo(chatRoomId, null, roomTypeOf(chatRoomType))

                    view.showRoomInfo(genericRoomToChatRoom(room))
                }
            } catch (ex: Exception) {
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun leaveChat(chatRoomId: String, chatRoomType: String) {
        view.showLoading()

        launchUI(strategy) {
            try {
                client.leaveChat(chatRoomId, roomTypeOf(chatRoomType))
                view.onLeave()
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

    fun hideOrShowChat(chatRoomId: String, chatRoomType: String, hide: Boolean) {
        view.showLoading()

        launchUI(strategy) {
            try {
                client.hide(chatRoomId, roomTypeOf(chatRoomType), hide)
                view.onHide()
            }
            catch (ex: Exception) {
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

    fun toEditChatInfo(chatRoomId: String, chatRoomType: String) {
        navigator.toEditChatInfo(chatRoomId, chatRoomType)
    }

    private fun canEdit(roomRoles: List<String>?, permissions: List<Permission>): Boolean {
        val editRoom: Permission? = permissions.find { it.id == EDIT_ROOM }

        if (editRoom != null)
            if (roomRoles != null)
                for (role in roomRoles)
                    if (editRoom.roles.contains(role))
                        return true

        return false
    }

    private fun genericRoomToChatRoom(chatRoom: Room): ChatRoom {
        return ChatRoom(chatRoom.id, chatRoom.type, chatRoom.user, chatRoom.name!!,
                chatRoom.fullName, chatRoom.readonly, chatRoom.updatedAt, 0L, 0L,
                chatRoom.topic, chatRoom.description, chatRoom.announcement, false,
                false, false, false, 0L, null, false, null, null, chatRoom.lastMessage, client
        )
    }
}
