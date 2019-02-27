package chat.rocket.android.contacts.presentation

import android.content.Context
import android.content.Intent
import chat.rocket.android.R
import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.dynamiclinks.DynamicLinksForFirebase
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.GetAccountInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.ConnectionManager
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extension.orFalse
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.UserPresence
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.createDirectMessage
import chat.rocket.core.internal.rest.inviteViaEmail
import chat.rocket.core.internal.rest.inviteViaSMS
import chat.rocket.core.internal.rest.usersGetPresence
import chat.rocket.core.model.ChatRoom
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ContactsPresenter @Inject constructor(
        private val view: ContactsView,
        private val strategy: CancelStrategy,
        private val navigator: MainNavigator,
        private val dbManager: DatabaseManager,
        manager: ConnectionManager,
        private val serverInteractor: GetCurrentServerInteractor,
        private val getAccountInteractor: GetAccountInteractor
) {
    @Inject
    lateinit var dynamicLinksManager : DynamicLinksForFirebase

    private val client = manager.client
    private val chatRoomsInteractor = FetchChatRoomsInteractor(client,dbManager)

    fun openDirectMessageChatRoom(username: String) {

        view.hideSpinner()
        launchUI(strategy) {
            try {
                val openedChatRooms = chatRoomByName(name = username, dbManager = dbManager)
                val chatRoom: ChatRoom? = openedChatRooms.firstOrNull()
                if (chatRoom == null) {
                    createDirectMessage(id = username)
                } else {
                    toDirectMessage(chatRoom)
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    private fun createDirectMessage(id: String) = launchUI(strategy) {
        try {
            val result = retryIO("createDirectMessage($id") {
                client.createDirectMessage(username = id)
            }

            chatRoomsInteractor.refreshChatRooms()

            val chatRoom = ChatRoom(
                    id = result.id,
                    type = roomTypeOf(RoomType.DIRECT_MESSAGE),
                    name = id,
                    fullName = id,
                    favorite = false,
                    open = false,
                    alert = false,
                    status = null,
                    client = client,
                    broadcast = false,
                    archived = false,
                    default = false,
                    description = null,
                    groupMentions = null,
                    userMentions = null,
                    lastMessage = null,
                    lastSeen = null,
                    topic = null,
                    announcement = null,
                    roles = null,
                    unread = 0,
                    readonly = false,
                    muted = null,
                    subscriptionId = "",
                    timestamp = null,
                    updatedAt = result.updatedAt,
                    user = null
            )

            withContext(CommonPool + strategy.jobs) {
                dbManager.chatRoomDao().insertOrReplace(chatRoom = ChatRoomEntity(
                        id = chatRoom.id,
                        name = chatRoom.name,
                        type = chatRoom.type.toString(),
                        fullname = chatRoom.fullName,
                        subscriptionId = chatRoom.subscriptionId,
                        updatedAt = chatRoom.updatedAt
                ))
            }

            toDirectMessage(chatRoom)
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    private fun toDirectMessage(chatRoom: ChatRoom) {
        navigator.toChatRoom(
                chatRoomId = chatRoom.id,
                chatRoomName = chatRoom.name,
                chatRoomType = chatRoom.type.toString(),
                isReadOnly = chatRoom.readonly.orFalse(),
                chatRoomLastSeen = chatRoom.lastSeen ?: 0,
                isSubscribed = chatRoom.open,
                isCreator = false,
                isFavorite = chatRoom.favorite
        )
    }

    private suspend fun chatRoomByName(name: String? = null, dbManager: DatabaseManager ): List<ChatRoom> = withContext(CommonPool) {
        return@withContext dbManager.chatRoomDao().getAllSync().filter {
            if (name == null) {
                return@filter true
            }
            it.chatRoom.name == name || it.chatRoom.fullname == name
        }.map {
            with(it.chatRoom) {
                ChatRoom(
                        id = id,
                        subscriptionId = subscriptionId,
                        type = roomTypeOf(type),
                        unread = unread,
                        broadcast = broadcast ?: false,
                        alert = alert,
                        fullName = fullname,
                        name = name ?: "",
                        favorite = favorite ?: false,
                        default = isDefault ?: false,
                        readonly = readonly,
                        open = open,
                        lastMessage = null,
                        archived = false,
                        status = null,
                        user = null,
                        userMentions = userMentions,
                        client = client,
                        announcement = null,
                        description = null,
                        groupMentions = groupMentions,
                        roles = null,
                        topic = null,
                        lastSeen = this.lastSeen,
                        timestamp = timestamp,
                        updatedAt = updatedAt
                )
            }
        }
    }

    fun inviteViaEmail(email:String) {
        launchUI(strategy) {
            try {
                val result:Boolean = retryIO("inviteViaEmail") { client.inviteViaEmail(email, Locale.getDefault().getLanguage()) }
                if (result) {
                    view.showMessage("Invitation Email Sent")
                } else{
                    view.showMessage("Failed to send Invitation Email")
                }
            } catch (ex: Exception) {
                when (ex) {
                    is RocketChatAuthException -> {
                        throw RocketChatAuthException("Not authenticated...")
                    }
                    else -> {
                        Timber.d(ex, "Error while inviting via email")
                        ex.message?.let {
                            view.showMessage(it)
                        }.ifNull {
                            view.showGenericErrorMessage()
                        }
                    }
                }
            }
        }
    }

    fun inviteViaSMS(phone:String) {
        launchUI(strategy) {
            try {
                val result:Boolean = retryIO("inviteViaSMS") { client.inviteViaSMS(phone, Locale.getDefault().getLanguage()) }
                if (result) {
                    view.showMessage("Invitation SMS Sent")
                } else{
                    view.showMessage("Failed to send Invitation SMS")
                }
            } catch (ex: Exception) {
                when (ex) {
                    is RocketChatAuthException -> {
                        throw RocketChatAuthException("Not authenticated...")
                    }
                    else -> {
                        Timber.d(ex, "Error while inviting via SMS")
                        ex.message?.let {
                            view.showMessage(it)
                        }.ifNull {
                            view.showGenericErrorMessage()
                        }
                    }
                }
            }
        }
    }

    fun shareViaApp(context: Context){
        launch {
            //get serverUrl and username
            val server = serverInteractor.get()!!
            val account = getAccountInteractor.get(server)!!
            val userName = account.userName

            var deepLinkCallback = { returnedString: String? ->
                with(Intent(Intent.ACTION_SEND)) {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.msg_check_this_out))
                    putExtra(Intent.EXTRA_TEXT, "Default Invitation Text : $returnedString")
                    context.startActivity(Intent.createChooser(this, context.getString(R.string.msg_share_using)))
                }
            }
            dynamicLinksManager.createDynamicLink(userName, server, deepLinkCallback)
        }
    }

    suspend fun getUserPresence(userId: String): UserPresence? {
        try {
            return retryIO { client.usersGetPresence(userId) }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
        return null
    }

}