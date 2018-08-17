package chat.rocket.android.main.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.uimodel.NavHeaderUiModel
import chat.rocket.android.main.uimodel.NavHeaderUiModelMapper
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.server.domain.GetAccountsInteractor
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
import chat.rocket.android.util.extensions.registerPushToken
import chat.rocket.android.util.extensions.serverLogoUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.UserStatus
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.logout
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.unregisterPushToken
import chat.rocket.core.model.Myself
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import javax.inject.Inject

class MainPresenter @Inject constructor(
    private val view: MainView,
    private val strategy: CancelStrategy,
    private val navigator: MainNavigator,
    private val tokenRepository: TokenRepository,
    private val serverInteractor: GetCurrentServerInteractor,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val refreshPermissionsInteractor: RefreshPermissionsInteractor,
    private val localRepository: LocalRepository,
    private val navHeaderMapper: NavHeaderUiModelMapper,
    private val saveAccountInteractor: SaveAccountInteractor,
    private val getAccountsInteractor: GetAccountsInteractor,
    private val removeAccountInteractor: RemoveAccountInteractor,
    private val factory: RocketChatClientFactory,
    private val groupedPush: GroupedPush,
    dbManagerFactory: DatabaseManagerFactory,
    getSettingsInteractor: GetSettingsInteractor,
    managerFactory: ConnectionManagerFactory
) : CheckServerPresenter(strategy, factory, view = view) {
    private val currentServer = serverInteractor.get()!!
    private val manager = managerFactory.create(currentServer)
    private val dbManager = dbManagerFactory.create(currentServer)
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
                    is RocketChatAuthException -> {
                        logout()
                    }
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
     * Logout from current server.
     */
    fun logout() {
        launchUI(strategy) {
            view.showProgress()
            try {
                clearTokens()
                retryIO("logout") { client.logout() }
            } catch (exception: RocketChatException) {
                Timber.d(exception, "Error calling logout")
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }

            try {
                disconnect()
                removeAccountInteractor.remove(currentServer)
                tokenRepository.remove(currentServer)

                withContext(CommonPool) { dbManager.logout() }
                navigator.toNewServer()
            } catch (ex: Exception) {
                Timber.d(ex, "Error cleaning up the session...")
            }
            view.hideProgress()
        }
    }

    fun connect() {
        refreshSettingsInteractor.refreshAsync(currentServer)
        refreshPermissionsInteractor.refreshAsync(currentServer)
        manager.connect()
    }

    fun disconnect() {
        manager.removeUserDataChannel(userDataChannel)
        manager.disconnect()
    }

    fun changeServer(serverUrl: String) {
        if (currentServer != serverUrl) {
            navigator.toNewServer(serverUrl)
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

    suspend fun refreshToken(token: String?) {
        token?.let {
            localRepository.save(LocalRepository.KEY_PUSH_TOKEN, token)
            client.registerPushToken(token, getAccountsInteractor.get(), factory)
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

    private suspend fun clearTokens() {
        serverInteractor.clear()
        val pushToken = localRepository.get(LocalRepository.KEY_PUSH_TOKEN)
        if (pushToken != null) {
            try {
                retryIO("unregisterPushToken") { client.unregisterPushToken(pushToken) }
                view.invalidateToken(pushToken)
            } catch (ex: Exception) {
                Timber.d(ex, "Error unregistering push token")
            }
        }
        localRepository.clearAllFromServer(currentServer)
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
