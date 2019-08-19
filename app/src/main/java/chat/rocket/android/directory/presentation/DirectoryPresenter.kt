package chat.rocket.android.directory.presentation

import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.directory.uimodel.DirectoryUiModelMapper
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.DirectoryRequestType
import chat.rocket.core.internal.rest.DirectoryWorkspaceType
import chat.rocket.core.internal.rest.createDirectMessage
import chat.rocket.core.internal.rest.directory
import chat.rocket.core.internal.rest.getInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class DirectoryPresenter @Inject constructor(
    private val view: DirectoryView,
    private val navigator: MainNavigator,
    private val strategy: CancelStrategy,
    @Named("currentServer") private val currentServer: String?,
    private val dbManager: DatabaseManager,
    val factory: RocketChatClientFactory,
    private val mapper: DirectoryUiModelMapper
) {
    private val client: RocketChatClient? = currentServer?.let { factory.get(it) }
    private var offset: Long = 0

    fun loadAllDirectoryChannels(query: String? = null) {
        launchUI(strategy) {
            try {
                view.showLoading()
                client?.directory(
                    text = query,
                    directoryRequestType = DirectoryRequestType.Channels(),
                    offset = offset,
                    count = 60
                )?.let {
                    val directoryUiModels = mapper.mapToUiModelList(it.result)
                    view.showChannels(directoryUiModels)
                    offset += 1 * 60L
                }
            } catch (exception: Exception) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun loadAllDirectoryUsers(isSearchForGlobalUsers: Boolean, query: String? = null) {
        launchUI(strategy) {
            try {
                view.showLoading()
                client?.directory(
                    text = query,
                    directoryRequestType = DirectoryRequestType.Users(),
                    directoryWorkspaceType = if (isSearchForGlobalUsers) {
                        DirectoryWorkspaceType.All()
                    } else {
                        DirectoryWorkspaceType.Local()
                    },
                    offset = offset,
                    count = 60
                )?.let {
                    val directoryUiModels = mapper.mapToUiModelList(it.result)
                    view.showUsers(directoryUiModels)
                    offset += 1 * 60L
                }
            } catch (exception: Exception) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun updateSorting(
        isSortByChannels: Boolean,
        isSearchForGlobalUsers: Boolean,
        query: String? = null
    ) {
        resetOffset()
        if (isSortByChannels) {
            loadAllDirectoryChannels(query)
        } else {
            loadAllDirectoryUsers(isSearchForGlobalUsers, query)
        }
    }

    fun toChannel(channelId: String, name: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                withContext(Dispatchers.Default) {
                    client?.getInfo(channelId, name, roomTypeOf(RoomType.CHANNEL))
                        ?.let { chatRoom ->
                            navigator.toChatRoom(
                                chatRoomId = channelId,
                                chatRoomName = name,
                                chatRoomType = RoomType.CHANNEL,
                                isReadOnly = chatRoom.readonly,
                                chatRoomLastSeen = -1,
                                isSubscribed = false,
                                isCreator = false,
                                isFavorite = false
                            )
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

    fun toDirectMessage(username: String, name: String) {
        launchUI(strategy) {
            try {
                view.showLoading()

                withContext(Dispatchers.Default) {
                    client?.createDirectMessage(username)?.let { directMessage ->
                        val chatRoomEntity = ChatRoomEntity(
                            id = directMessage.id,
                            parentId = null,
                            name = username,
                            description = null,
                            type = RoomType.DIRECT_MESSAGE,
                            fullname = name,
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

    private fun resetOffset() {
        offset = 0
    }
}