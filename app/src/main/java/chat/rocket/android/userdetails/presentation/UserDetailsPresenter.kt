package chat.rocket.android.userdetails.presentation

import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.db.model.UserEntity
import chat.rocket.android.server.domain.GetConnectingServerInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.model.RoomType
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.createDirectMessage
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import javax.inject.Inject

class UserDetailsPresenter @Inject constructor(
    private val view: UserDetailsView,
    private val dbManager: DatabaseManager,
    private val strategy: CancelStrategy,
    private val navigator: ChatRoomNavigator,
    serverInteractor: GetConnectingServerInteractor,
    factory: ConnectionManagerFactory
) {
    private var currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client
    private val interactor = FetchChatRoomsInteractor(client, dbManager)
    private lateinit var userEntity: UserEntity

    fun loadUserDetails(userId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                dbManager.getUser(userId)?.let {
                    userEntity = it
                    val avatarUrl =
                        userEntity.username?.let { currentServer.avatarUrl(avatar = it) }
                    val username = userEntity.username
                    val name = userEntity.name
                    val utcOffset =
                        userEntity.utcOffset // TODO Convert UTC and display like the mockup

                    if (avatarUrl != null && username != null && name != null && utcOffset != null) {
                        view.showUserDetails(
                            avatarUrl = avatarUrl,
                            name = name,
                            username = username,
                            status = userEntity.status,
                            utcOffset = utcOffset.toString()
                        )
                    } else {
                        throw Exception()
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

    fun createDirectMessage(username: String) {
        launchUI(strategy) {
            try {
                view.showLoading()

                withContext(DefaultDispatcher) {
                    val directMessage = retryIO("createDirectMessage($username") {
                        client.createDirectMessage(username)
                    }

                    val chatRoomEntity = ChatRoomEntity(
                        id = directMessage.id,
                        name = userEntity.username ?: userEntity.name.orEmpty(),
                        description = null,
                        type = RoomType.DIRECT_MESSAGE,
                        fullname = userEntity.name,
                        subscriptionId = "",
                        updatedAt = directMessage.updatedAt
                    )

                    dbManager.insertOrReplaceRoom(chatRoomEntity)

                    interactor.refreshChatRooms()

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
}
