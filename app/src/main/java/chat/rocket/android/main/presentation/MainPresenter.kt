package chat.rocket.android.main.presentation

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import chat.rocket.android.R
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.emoji.Emoji
import chat.rocket.android.emoji.EmojiRepository
import chat.rocket.android.emoji.Fitzpatrick
import chat.rocket.android.emoji.internal.EmojiCategory
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.uimodel.NavHeaderUiModel
import chat.rocket.android.main.uimodel.NavHeaderUiModelMapper
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.GetAccountInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.RefreshPermissionsInteractor
import chat.rocket.android.server.domain.RemoveAccountInteractor
import chat.rocket.android.server.domain.SaveAccountInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.favicon
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.server.presentation.CheckServerPresenter
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.adminPanelUrl
import chat.rocket.android.util.extensions.serverLogoUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.UserStatus
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.getCustomEmojis
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.unregisterPushToken
import chat.rocket.core.internal.rest.inviteViaEmail
import chat.rocket.core.internal.rest.inviteViaSMS
import chat.rocket.core.model.Myself
import kotlinx.coroutines.experimental.channels.Channel
import timber.log.Timber
import javax.inject.Inject
import chat.rocket.android.util.extensions.showToast


class MainPresenter @Inject constructor(
    private val view: MainView,
    private val strategy: CancelStrategy,
    private val navigator: MainNavigator,
    private val tokenRepository: TokenRepository,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val refreshPermissionsInteractor: RefreshPermissionsInteractor,
    private val navHeaderMapper: NavHeaderUiModelMapper,
    private val saveAccountInteractor: SaveAccountInteractor,
    private val getAccountsInteractor: GetAccountsInteractor,
    private val getAccountInteractor: GetAccountInteractor,
    private val groupedPush: GroupedPush,
    private val serverInteractor: GetCurrentServerInteractor,
    localRepository: LocalRepository,
    removeAccountInteractor: RemoveAccountInteractor,
    factory: RocketChatClientFactory,
    dbManagerFactory: DatabaseManagerFactory,
    getSettingsInteractor: GetSettingsInteractor,
    managerFactory: ConnectionManagerFactory
) : CheckServerPresenter(
    strategy = strategy,
    factory = factory,
    serverInteractor = serverInteractor,
    localRepository = localRepository,
    removeAccountInteractor = removeAccountInteractor,
    tokenRepository = tokenRepository,
    managerFactory = managerFactory,
    dbManagerFactory = dbManagerFactory,
    tokenView = view,
    navigator = navigator
) {
    private val currentServer = serverInteractor.get()!!
    private val manager = managerFactory.create(currentServer)
    private val client: RocketChatClient = factory.create(currentServer)
    private var settings: PublicSettings = getSettingsInteractor.get(serverInteractor.get()!!)
    private val userDataChannel = Channel<Myself>()

    fun toChatList(chatRoomId: String? = null) = navigator.toChatList(chatRoomId)

    fun toUserProfile() = navigator.toUserProfile()

    fun toSettings() = navigator.toSettings()

    fun toAdminPanel() = tokenRepository.get(currentServer)?.let {
        navigator.toAdminPanel(currentServer.adminPanelUrl(), it.authToken)
    }

    fun toCreateChannel() = navigator.toCreateChannel()

    fun loadServerAccounts() {
        launchUI(strategy) {
            try {
                view.setupServerAccountList(getAccountsInteractor.get())
            } catch (ex: Exception) {
                when (ex) {
                    is RocketChatAuthException -> logout()
                    else -> {
                        Timber.d(ex, "Error loading serve accounts")
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

    fun loadCurrentInfo() {
        checkServerInfo(currentServer)
        launchUI(strategy) {
            try {
                val me = retryIO("me") { client.me() }
                val model = navHeaderMapper.mapToUiModel(me)
                saveAccount(model)
                view.setupUserAccountInfo(model)
            } catch (ex: Exception) {
                when (ex) {
                    is RocketChatAuthException -> logout()
                    else -> {
                        Timber.d(ex, "Error loading my information for navheader")
                        ex.message?.let {
                            view.showMessage(it)
                        }.ifNull {
                            view.showGenericErrorMessage()
                        }
                    }
                }
            }
            subscribeMyselfUpdates()
        }
    }

    /**
     * Load all emojis for the current server. Simple emojis are always the same for every server,
     * but custom emojis vary according to the its url.
     */
    fun loadEmojis() {
        launchUI(strategy) {
            EmojiRepository.setCurrentServerUrl(currentServer)
            val customEmojiList = mutableListOf<Emoji>()
            try {
                for (customEmoji in retryIO("getCustomEmojis()") { client.getCustomEmojis() }) {
                    customEmojiList.add(Emoji(
                        shortname = ":${customEmoji.name}:",
                        category = EmojiCategory.CUSTOM.name,
                        url = "$currentServer/emoji-custom/${customEmoji.name}.${customEmoji.extension}",
                        count = 0,
                        fitzpatrick = Fitzpatrick.Default.type,
                        keywords = customEmoji.aliases,
                        shortnameAlternates = customEmoji.aliases,
                        siblings = mutableListOf(),
                        unicode = "",
                        isDefault = true
                    ))
                }

                EmojiRepository.load(view as Context, customEmojis = customEmojiList)
            } catch (ex: RocketChatException) {
                Timber.e(ex)
                EmojiRepository.load(view as Context)
            }
        }
    }

    fun logout() {
        setupConnectionInfo(currentServer)
        super.logout(userDataChannel)
    }

     /**
     * Invite
     */
    fun invite(context: Context) {
        launchUI(strategy) {

            //get serverUrl and username
            val server = serverInteractor.get()!!
            val account = getAccountInteractor.get(server)!!
            val userName = account.userName

            val defaultMessage = "Hey! I’m on Veranda. If you sign up we can chat for free. \nMy username is “$userName” on server $server "

            //Dialog
            val layoutInflater = LayoutInflater.from(context)
            val dialogLayout = layoutInflater.inflate(R.layout.invite_dialog, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.invite_text)
            editText.setText(defaultMessage, TextView.BufferType.NORMAL)

            AlertDialog.Builder(context)
                    .setTitle(R.string.invite_label)
                    .setView(dialogLayout)
                    .setPositiveButton(R.string.action_invite) { dialog, _ ->
                        dialog.dismiss()

                        //intent
                        val inviteIntent = Intent()
                        inviteIntent.action = Intent.ACTION_SEND
                        inviteIntent.putExtra(Intent.EXTRA_TEXT, editText.text.toString())
                        inviteIntent.type = "text/plain"
                        startActivity(context, inviteIntent, null)
                    }.show()
        }
    }

    fun connect() {
        refreshSettingsInteractor.refreshAsync(currentServer)
        refreshPermissionsInteractor.refreshAsync(currentServer)
        manager.connect()
    }

    fun disconnect() {
        setupConnectionInfo(currentServer)
        super.disconnect(userDataChannel)
    }

    fun changeServer(serverUrl: String) {
        if (currentServer != serverUrl) {
            navigator.switchOrAddNewServer(serverUrl)
        } else {
            view.closeServerSelection()
        }
    }

    fun addNewServer() {
        navigator.toServerScreen()
    }

    fun changeDefaultStatus(userStatus: UserStatus) {
        launchUI(strategy) {
            try {
                manager.setDefaultStatus(userStatus)
                view.showUserStatus(userStatus)
            } catch (ex: RocketChatException) {
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun inviteViaEmail(email:String) {
        launchUI(strategy) {
            try {
                val result:Boolean = retryIO("inviteViaEmail") { client.inviteViaEmail(email) }
                if (result) {
                    view.showMessage("Invitation Email Sent")
                } else{
                    view.showMessage("Failed to send Invitation Email")
                }
            } catch (ex: Exception) {
                when (ex) {
                    is RocketChatAuthException -> {
                        logout()
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
                val result:Boolean = retryIO("inviteViaSMS") { client.inviteViaSMS(phone) }
                if (result) {
                    view.showMessage("Invitation SMS Sent")
                } else{
                    view.showMessage("Failed to send Invitation SMS")
                }
            } catch (ex: Exception) {
                when (ex) {
                    is RocketChatAuthException -> {
                        logout()
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

    private suspend fun saveAccount(uiModel: NavHeaderUiModel) {
        val icon = settings.favicon()?.let {
            currentServer.serverLogoUrl(it)
        }
        val account = Account(
            currentServer,
            icon,
            uiModel.serverLogo,
            uiModel.userDisplayName!!,
            uiModel.userAvatar
        )
        saveAccountInteractor.save(account)
    }

    private suspend fun subscribeMyselfUpdates() {
        manager.addUserDataChannel(userDataChannel)
        for (myself in userDataChannel) {
            updateMyself(myself)
        }
    }

    private fun updateMyself(myself: Myself) =
        view.setupUserAccountInfo(navHeaderMapper.mapToUiModel(myself))

    fun clearNotificationsForChatroom(chatRoomId: String?) {
        if (chatRoomId == null) return

        groupedPush.hostToPushMessageList[currentServer]?.let { list ->
            list.removeAll { it.info.roomId == chatRoomId }
        }
    }
}
