package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.SortingAndGroupingInteractor
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.server.domain.useSpecialCharsOnRoom
import chat.rocket.android.server.infrastructure.ConnectionManager
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryDB
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.User
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.internal.realtime.createDirectMessage
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.show
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
    private val sortingAndGroupingInteractor: SortingAndGroupingInteractor,
    private val dbManager: DatabaseManager,
    manager: ConnectionManager,
    private val localRepository: LocalRepository,
    private val userHelper: UserHelper,
    settingsRepository: SettingsRepository
) {
    private val client = manager.client
    private val settings = settingsRepository.get(currentServer)

    fun toCreateChannel() = navigator.toCreateChannel()

    fun toSettings() = navigator.toSettings()

    fun toDirectory() = navigator.toDirectory()

    fun getCurrentServerName() = view.setupToolbar(currentServer)

    fun getSortingAndGroupingPreferences() {
        with(sortingAndGroupingInteractor) {
            view.setupSortingAndGrouping(
                getSortByName(currentServer),
                getUnreadOnTop(currentServer),
                getGroupByType(currentServer),
                getGroupByFavorites(currentServer)
            )
        }
    }

    fun loadChatRoom(roomId: String) {
        launchUI(strategy) {
            try {
                val room = dbManager.getRoom(roomId)
                if (room != null) {
                    loadChatRoom(room.chatRoom, true)
                } else {
                    Timber.e("Error loading channel")
                    view.showGenericErrorMessage()
                }
            } catch (ex: Exception) {
                Timber.e(ex, "Error loading channel")
                view.showGenericErrorMessage()
            }
        }
    }

    fun loadChatRoom(chatRoom: RoomUiModel) {
        launchUI(strategy) {
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
                Timber.e(ex, "Error loading channel")
                view.showGenericErrorMessage()
            }
        }
    }

    suspend fun loadChatRoom(chatRoom: ChatRoomEntity, local: Boolean = false) {
        with(chatRoom) {
            val isDirectMessage = roomTypeOf(type) is RoomType.DirectMessage
            val roomName =
                if (settings.useSpecialCharsOnRoom() || (isDirectMessage && settings.useRealName())) {
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