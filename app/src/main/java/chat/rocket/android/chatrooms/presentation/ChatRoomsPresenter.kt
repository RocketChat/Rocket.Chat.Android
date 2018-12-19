package chat.rocket.android.chatrooms.presentation

import android.util.Log
import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.db.model.FullMessage
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.server.domain.useSpecialCharsOnRoom
import chat.rocket.android.server.infraestructure.ConnectionManager
import chat.rocket.android.server.infraestructure.DatabaseMessageMapper
import chat.rocket.android.server.infraestructure.DatabaseMessagesRepository
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryDB
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.User
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.realtime.createDirectMessage
import chat.rocket.core.internal.rest.*
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatRoomsPresenter @Inject constructor(
    private val view: ChatRoomsView,
    private val strategy: CancelStrategy,
    private val navigator: MainNavigator,
    @Named("currentServer") private val currentServer: String,
    private val dbManager: DatabaseManager,
    manager: ConnectionManager,
    private val localRepository: LocalRepository,
    private val userHelper: UserHelper,
    settingsRepository: SettingsRepository
) {
    private val client = manager.client
    private val settings = settingsRepository.get(currentServer)

    fun loadChatRoom(roomId: String) {
        launchUI(strategy) {
            view.showLoadingRoom("")
            try {
                val room = dbManager.getRoom(roomId)
                if (room != null) {
                    loadChatRoom(room.chatRoom, true)
                } else {
                    Timber.d("Error loading channel")
                    view.showGenericErrorMessage()
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error loading channel")
                view.showGenericErrorMessage()
            } finally {
                view.hideLoadingRoom()
            }
        }
    }

    fun loadChatRoom(chatRoom: RoomUiModel) {
        launchUI(strategy) {
            view.showLoadingRoom(chatRoom.name)
            try {
                val room = retryDB("getRoom(${chatRoom.id}") { dbManager.getRoom(chatRoom.id) }
                if (room != null) {
                    loadChatRoom(room.chatRoom, true)
                } else {
                    with(chatRoom) {
                        val entity = ChatRoomEntity(
                            id = id,
                            subscriptionId = "",
                            type = type.toString(),
                            name = username ?: name.toString(),
                            fullname = name.toString(),
                            open = open,
                            muted = muted
                        )
                        loadChatRoom(entity, false)
                    }
                }
            } catch (ex: Exception) {
                Timber.d(ex, "Error loading channel")
                view.showGenericErrorMessage()
            } finally {
                view.hideLoadingRoom()
            }
        }
    }

    suspend fun loadChatRoom(chatRoom: ChatRoomEntity, local: Boolean = false) {
        with(chatRoom) {
            val isDirectMessage = roomTypeOf(type) is RoomType.DirectMessage
            val roomName = if (settings.useSpecialCharsOnRoom() || (isDirectMessage && settings.useRealName())) {
                fullname ?: name
            } else {
                name
            }

            val myself = getCurrentUser()
            if (myself?.username == null) {
                view.showMessage(R.string.msg_generic_error)
            } else {
                val id = if (isDirectMessage && !open) {
                    // If from local database, we already have the roomId, no need to concatenate
                    if (local) {
                        retryIO {
                            client.show(id, roomTypeOf(RoomType.DIRECT_MESSAGE))
                        }
                        id
                    } else {
                        retryIO("createDirectMessage($name)") {
                            withTimeout(10000) {
                                createDirectMessage(name)
                                FetchChatRoomsInteractor(client, dbManager).refreshChatRooms()
                            }
                        }
                        val fromTo = mutableListOf(myself.id, id).apply {
                            sort()
                        }
                        fromTo.joinToString("")
                    }
                } else {
                    id
                }

                navigator.toChatRoom(
                        chatRoomId = id,
                        chatRoomName = roomName,
                        chatRoomType = type,
                        isReadOnly = readonly ?: false,
                        chatRoomLastSeen = lastSeen ?: -1,
                        isSubscribed = open,
                        isCreator = ownerId == myself.id || isDirectMessage,
                        isFavorite = favorite ?: false
                )
            }
        }
    }

    fun toggleFavoriteChatRoom(roomId: String, isFavorite: Boolean) {
        launchUI(strategy) {
            try {
                // Note that if it is favorite then the user wants to unfavorite - and vice versa.
                retryIO("favorite($roomId, $isFavorite)") {
                    client.favorite(roomId, !isFavorite)
                  val rooms = retryIO("fetch chatRooms", times = 10,
                      initialDelay = 200, maxDelay = 2000) {
                    client.chatRooms().update
                  }
                  Timber.d("Refreshing rooms: $rooms")
                  dbManager.processRooms(rooms)
                }
            } catch (e: RocketChatException) {
                Timber.e(e, "Error while trying to favorite/unfavorite chat room.")
                e.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun toggleMarkRead(roomId: String, unread: String?) {
        launchUI(strategy) {
            try {
                if(unread.isNullOrEmpty()) {
                    retryIO(description = "markAsUnread($roomId)") { client.markAsUnread(roomId) }
                  val rooms = retryIO("fetch chatRooms", times = 10,
                      initialDelay = 200, maxDelay = 2000) {
                    client.chatRooms().update
                  }
                  Timber.d("Refreshing rooms: $rooms")
                  dbManager.processRooms(rooms)
                }
                else
                    retryIO(description = "markAsRead($roomId)") { client.markAsRead(roomId) }
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    fun leaveChatRoom(roomId: String, roomType: RoomType) {
        launchUI(strategy) {
            try {
                retryIO(description = "leaveChat($roomId, $roomType)") { client.leaveChat(roomId, roomType) }
                val rooms = retryIO("fetch chatRooms", times = 10,
                    initialDelay = 200, maxDelay = 2000) {
                    client.chatRooms().update
                }
                Timber.d("Refreshing rooms: $rooms")
                dbManager.processRooms(rooms)
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    fun hideRoom(roomId: String, roomType: RoomType) {
        launchUI(strategy) {
            try {
                retryIO(description = "hide($roomId, $roomType, ${true})") { client.hide(roomId, roomType, hideRoom = true) }
                val rooms = retryIO("fetch chatRooms", times = 10,
                    initialDelay = 200, maxDelay = 2000) {
                    client.chatRooms().update
                }
                Timber.d("Refreshing rooms: $rooms")
                dbManager.processRooms(rooms)
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    private suspend fun getCurrentUser(): User? {
        userHelper.user()?.let {
            return it
        }
        try {
            val myself = retryIO { client.me() }
            val user = User(
                id = myself.id,
                username = myself.username,
                name = myself.name,
                status = myself.status,
                utcOffset = myself.utcOffset,
                emails = null,
                roles = myself.roles
            )
            localRepository.saveCurrentUser(url = currentServer, user = user)
        } catch (ex: RocketChatException) {
            Timber.e(ex)
        }
        return null
    }

    private suspend fun createDirectMessage(name: String): Boolean = suspendCoroutine { cont ->
        client.createDirectMessage(name) { success, _ ->
            cont.resume(success)
        }
    }
}