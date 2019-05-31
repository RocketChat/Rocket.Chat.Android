package chat.rocket.android.contacts.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.helper.Constants
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.helper.SharedPreferenceHelper
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
import chat.rocket.android.contacts.models.Contact
import chat.rocket.android.helper.ShareAppHelper
import chat.rocket.core.internal.rest.*
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.SpotlightResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ContactsPresenter @Inject constructor(
        private val view: ContactsView,
        private val strategy: CancelStrategy,
        private val navigator: MainNavigator,
        private val dbManager: DatabaseManager,
        private val analyticsManager: AnalyticsManager,
        manager: ConnectionManager,
        private val serverInteractor: GetCurrentServerInteractor,
        private val getAccountInteractor: GetAccountInteractor,
        val userHelper: UserHelper
) {
    @Inject
    lateinit var shareAppHelper: ShareAppHelper

    private val client = manager.client
    private val chatRoomsInteractor = FetchChatRoomsInteractor(client,dbManager)
    private val user = userHelper.user()

    fun openDirectMessageChatRoom(username: String) {
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

            withContext(Dispatchers.IO + strategy.jobs) {
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

    private suspend fun chatRoomByName(name: String? = null, dbManager: DatabaseManager ): List<ChatRoom> = withContext(Dispatchers.IO) {
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

    fun inviteWithRealNamePrompt(contact: Contact, context: Context?) {

        fun invite(contact: Contact, realname: String? = null) {
            if (contact.isPhone()) inviteViaSMS(contact.getPhoneNumber()!!, realname)
            else inviteViaEmail(contact.getEmailAddress()!!, realname)
        }

        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.widechat_real_username_dialog, null)
        val dialog = AlertDialog.Builder(context as Context)
                .setView(view)
                .setPositiveButton(null,null)
                .setNegativeButton(null,null)
                .create()

        val positiveButton = view.findViewById(R.id.positive_button) as Button
        val negativeButton = view.findViewById(R.id.negative_button) as Button
        var realUsername = view.findViewById(R.id.editTextDialogUserInput) as EditText

        if (!SharedPreferenceHelper.getString(Constants.WIDECHAT_REAL_USER_NAME, "").isNullOrEmpty()) {
            realUsername.setText(SharedPreferenceHelper.getString(Constants.WIDECHAT_REAL_USER_NAME, ""))
        }

        positiveButton.setOnClickListener(View.OnClickListener {
            SharedPreferenceHelper.putString(Constants.WIDECHAT_REAL_USER_NAME, realUsername.getText().toString())
            invite(contact, realUsername.getText().toString())
            dialog.dismiss()
        })
        negativeButton.setOnClickListener(View.OnClickListener {
            invite(contact)
            dialog.dismiss()
        })
        dialog.show()
    }

    fun inviteViaEmail(email: String, realname: String?) {
        launchUI(strategy) {
            try {
                val result:Boolean = retryIO("inviteViaEmail") { client.inviteViaEmail(email, Locale.getDefault().getLanguage(), realname) }
                if (result) {
                    analyticsManager.logInviteSent("email", true)
                    view.showMessage("Invitation Email Sent")
                } else{
                    analyticsManager.logInviteSent("email", false)
                    view.showMessage("Failed to send Invitation Email")
                }
            } catch (ex: Exception) {
                analyticsManager.logInviteSent("email", false)
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

    fun inviteViaSMS(phone: String, realname: String?) {
        launchUI(strategy) {
            try {
                val result:Boolean = retryIO("inviteViaSMS") { client.inviteViaSMS(phone, Locale.getDefault().getLanguage(), realname) }
                if (result) {
                    analyticsManager.logInviteSent("sms", true)
                    view.showMessage("Invitation SMS Sent")
                } else{
                    analyticsManager.logInviteSent("sms", false)
                    view.showMessage("Failed to send Invitation SMS")
                }
            } catch (ex: Exception) {
                analyticsManager.logInviteSent("sms", false)
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
        shareAppHelper.shareViaApp(context)
    }

    suspend fun getUserPresence(userId: String): UserPresence? {
        try {
            return retryIO { client.usersGetPresence(userId) }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
        return null
    }

    suspend fun spotlight(query: String): SpotlightResult {
        return client.spotlight(query)
    }

}