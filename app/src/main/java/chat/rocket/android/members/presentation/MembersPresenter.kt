package chat.rocket.android.members.presentation

import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import android.util.Log
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.members.uimodel.MemberUiModelMapper
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.isNotNullNorEmpty
import chat.rocket.android.util.retryDB
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.*
import chat.rocket.core.model.Command
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class MembersPresenter @Inject constructor(
    private val view: MembersView,
    private val navigator: ChatRoomNavigator,
    private val dbManager: DatabaseManager,
    @Named("currentServer") private val currentServer: String,
    private val strategy: CancelStrategy,
    private val mapper: MemberUiModelMapper,
    val factory: RocketChatClientFactory,
    private val userHelper: UserHelper
) {
    private val client: RocketChatClient = factory.create(currentServer)
    private var offset: Long = 0
    private lateinit var roomType: RoomType
    private lateinit var roomId: String
    private var totalMembers: Long = 0
    var isRoomOwner: Boolean = false

    /**
     * Loads all the chat room members for the given room id.
     *
     * @param roomId The id of the room to get chat room members from.
     */
    fun loadChatRoomsMembers(roomId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                dbManager.getRoom(roomId)?.let {
                    val members = client.getMembers(roomId, roomTypeOf(it.chatRoom.type), offset, 60)
                    this@MembersPresenter.roomId = it.chatRoom.id
                    this@MembersPresenter.roomType = roomTypeOf(it.chatRoom.type)
                    val chatRoomRole = if (roomType !is RoomType.DirectMessage) {
                            client.chatRoomRoles(roomType = roomType, roomName = it.chatRoom.name)
                        } else {
                            emptyList()
                        }
                    val muted = it.chatRoom.muted
                    val currentUserId = client.me().id

                    val memberUiModels = mapper.mapToUiModelList(members.result)
                    memberUiModels.forEach {
                        val userId = it.userId
                        val username = it.username
                        it.roles = chatRoomRole.find { it.user.id ==  userId}?.roles
                        if (it.userId == currentUserId) this@MembersPresenter.isRoomOwner = it.roles?.contains("owner") == true
                        it.muted = muted?.find { it == username }.isNotNullNorEmpty()
                    }
                    view.showMembers(memberUiModels, members.total)
                    totalMembers = members.total
                    offset += 1 * 60L
                }.ifNull {
                    Timber.e("Couldn't find a room with id: $roomId at current server.")
                }
            } catch (exception: RocketChatException) {
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

    fun toMemberDetails(memberUiModel: MemberUiModel) {
        with(memberUiModel) {
            if (userId != userHelper.user()?.id) {
                navigator.toMemberDetails(userId)
            }
        }
    }

    fun toggleOwner(userId: String, isOwner: Boolean = false, notifier: () ->Unit) {
        launchUI(strategy) {
            try {
                if (isOwner)
                    retryIO(description = "removeOwner($roomId, $roomType, $userId)") { client.removeOwner(roomId, roomType, userId) }
                else
                    retryIO(description = "addOwner($roomId, $roomType, $userId)") { client.addOwner(roomId, roomType, userId) }
                notifier()
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    fun toggleLeader(userId: String, isLeader: Boolean = false, notifier: () ->Unit) {
        launchUI(strategy) {
            try {
                if (isLeader)
                    retryIO(description = "removeLeader($roomId, $roomType, $userId)") { client.removeLeader(roomId, roomType, userId) }
                else
                    retryIO(description = "addLeader($roomId, $roomType, $userId)") { client.addLeader(roomId, roomType, userId) }
                notifier()
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    fun toggleModerator(userId: String, isModerator: Boolean = false, notifier: () ->Unit) {
        launchUI(strategy) {
            try {
                if (isModerator)
                    retryIO(description = "removeModerator($roomId, $roomType, $userId)") { client.removeModerator(roomId, roomType, userId) }
                else
                    retryIO(description = "addModerator($roomId, $roomType, $userId)") { client.addModerator(roomId, roomType, userId) }
                notifier()
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    fun toggleIgnore(userId: String, isIgnored: Boolean = false, notifier: () ->Unit) {
        launchUI(strategy) {
            try {
                    retryIO(description = "ignoreUser($roomId, $userId, ${!isIgnored})") { client.ignoreUser(roomId, userId, !isIgnored) }
                notifier()
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    fun toggleMute(username: String?, isMuted: Boolean = false,  notifier: () ->Unit) {
        launchUI(strategy) {
            try {
                if (isMuted)
                    retryIO("runCommand(unmute, $username, $roomId)") {
                        client.runCommand(Command("unmute", username), roomId)
                    }
                else
                    retryIO("runCommand(mute, $username, $roomId)") {
                        client.runCommand(Command("mute", username), roomId)
                    }
                notifier()
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

    fun removeUser(userId: String,  notifier: () ->Unit) {
        launchUI(strategy) {
            try {
                    retryIO(description = "removeUser($roomId, $roomType, $userId)") { client.removeUser(roomId, roomType, userId) }
                notifier()
                view.setMemberCount(--totalMembers)
            } catch (ex: RocketChatException) {
                view.showMessage(ex.message!!) // TODO Remove.
                Timber.e(ex) // FIXME: Right now we are only catching the exception with Timber.
            }
        }
    }

}
