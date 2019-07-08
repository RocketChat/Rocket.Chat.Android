package chat.rocket.android.userdetails.presentation

import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.db.model.UserEntity
import chat.rocket.android.server.domain.CurrentServerRepository
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.domain.REMOVE_USER
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.isJitsiEnabled
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.createDirectMessage
import chat.rocket.core.internal.rest.kick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UserDetailsPresenter @Inject constructor(
    private val view: UserDetailsView,
    private val dbManager: DatabaseManager,
    private val strategy: CancelStrategy,
    private val navigator: ChatRoomNavigator,
    private val permissionsInteractor: PermissionsInteractor,
    tokenRepository: TokenRepository,
    settingsInteractor: GetSettingsInteractor,
    serverInteractor: CurrentServerRepository,
    factory: ConnectionManagerFactory
) {
    private var currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager?.client
    private val settings = settingsInteractor.get(currentServer)
    private val token = tokenRepository.get(currentServer)
    private lateinit var userEntity: UserEntity

    fun loadUserDetails(userId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                dbManager.getUser(userId)?.let {
                    userEntity = it
                    val avatarUrl = userEntity.username?.let { username ->
                        currentServer.avatarUrl(username, token?.userId, token?.authToken)
                    }
                    val username = userEntity.username
                    val name = userEntity.name
                    val utcOffset = userEntity.utcOffset // FIXME Convert UTC

                    view.showUserDetailsAndActions(
                        avatarUrl = avatarUrl,
                        name = name,
                        username = username,
                        status = userEntity.status,
                        utcOffset = utcOffset.toString(),
                        isVideoCallAllowed = settings.isJitsiEnabled()
                    )
                }
            } catch (ex: Exception) {
                Timber.e(ex)
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

    fun getImageUri(): String {
        return userEntity.username?.let {
            currentServer.avatarUrl(
                avatar = it, userId = token?.userId,
                token = token?.authToken
            )
        }!!
    }

    fun createDirectMessage(username: String) {
        launchUI(strategy) {
            try {
                view.showLoading()

                withContext(Dispatchers.Default) {
                    retryIO("createDirectMessage($username") {
                        client?.createDirectMessage(username)?.let { directMessage ->
                            val chatRoomEntity = ChatRoomEntity(
                                id = directMessage.id,
                                name = userEntity.username ?: userEntity.name.orEmpty(),
                                parentId = null,
                                description = null,
                                type = RoomType.DIRECT_MESSAGE,
                                fullname = userEntity.name,
                                subscriptionId = "",
                                updatedAt = directMessage.updatedAt
                            )

                            dbManager.insertOrReplaceRoom(chatRoomEntity)

                            FetchChatRoomsInteractor(client, dbManager).refreshChatRooms()

                            navigator.toChatRoom(
                                chatRoomId = chatRoomEntity.id,
                                chatRoomName = chatRoomEntity.name,
                                chatRoomType = chatRoomEntity.type,
                                isReadOnly = false,
                                chatRoomLastSeen = -1,
                                isSubscribed = chatRoomEntity.open,
                                isCreator = true,
                                isFavorite = false
                            )
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex)
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

    fun toVideoConference(username: String) {
        launchUI(strategy) {
            try {
                withContext(Dispatchers.Default) {
                    retryIO("createDirectMessage($username") {
                        client?.createDirectMessage(username)?.let { directMessage ->
                            navigator.toVideoConference(directMessage.id, RoomType.DIRECT_MESSAGE)
                        }
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex)
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun removeUser(userId: String, chatRoomId: String) {
        launchUI(strategy) {
            try {
                dbManager.getRoom(chatRoomId)?.let {
                    retryIO("kick($chatRoomId,${roomTypeOf(it.chatRoom.type)},$userId)") {
                        client?.kick(chatRoomId, roomTypeOf(it.chatRoom.type), userId)
                            ?.let { isUsersRemoved ->
                                if (isUsersRemoved) {
                                    view.showUserRemovedMessage()
                                }
                            }
                    }
                }.ifNull {
                    Timber.e("Couldn't find a room with id: $chatRoomId at current server.")
                }
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

    fun checkRemoveUserPermission(chatRoomId: String) {
        launchUI(strategy) {
            if (hasRemoveUserPermission(chatRoomId)) {
                view.showRemoveUserButton()
            } else {
                view.hideRemoveUserButton()
            }
        }
    }

    private suspend fun hasRemoveUserPermission(chatRoomId: String): Boolean {
        return permissionsInteractor.hasPermission(REMOVE_USER, chatRoomId)
    }

    fun toProfileImage(avatarUrl: String) = navigator.toProfileImage(avatarUrl)
}

