package chat.rocket.android.main.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.RefreshPermissionsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.RemoveAccountInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.server.presentation.CheckServerPresenter
import chat.rocket.android.util.extension.launchUI
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.util.ifNull
import timber.log.Timber
import javax.inject.Inject

class MainPresenter @Inject constructor(
    private val mainView: MainView,
    private val cancelStrategy: CancelStrategy,
    private val mainNavigator: MainNavigator,
    val getCurrentServerInteractor: GetCurrentServerInteractor,
    private val groupedPush: GroupedPush,
    private val getAccountsInteractor: GetAccountsInteractor,
    rocketChatClientFactory: RocketChatClientFactory,
    localRepository: LocalRepository,
    removeAccountInteractor: RemoveAccountInteractor,
    tokenRepository: TokenRepository,
    private val connectionManagerFactory: ConnectionManagerFactory,
    databaseManagerFactory: DatabaseManagerFactory,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val refreshPermissionsInteractor: RefreshPermissionsInteractor


/*

    private val navHeaderMapper: NavHeaderUiModelMapper,
    private val saveAccountInteractor: SaveAccountInteractor,
    private val getAccountsInteractor: GetAccountsInteractor,
    private val groupedPush: GroupedPush,
    serverInteractor: GetCurrentServerInteractor,
    getSettingsInteractor: GetSettingsInteractor,

 */

) : CheckServerPresenter(
    strategy = cancelStrategy,
    factory = rocketChatClientFactory,
    serverInteractor = getCurrentServerInteractor,
    localRepository = localRepository,
    removeAccountInteractor = removeAccountInteractor,
    tokenRepository = tokenRepository,
    managerFactory = connectionManagerFactory,
    dbManagerFactory = databaseManagerFactory,
    tokenView = mainView,
    navigator = mainNavigator
) {

    fun clearNotificationsForChatRoom(chatRoomId: String?) {
        if (chatRoomId == null) return

        getCurrentServerInteractor.get()?.let { currentServer ->
            groupedPush.hostToPushMessageList[currentServer].let { list ->
                list?.removeAll { it.info.roomId == chatRoomId }
            }
        }
    }

    fun connect() {
        getCurrentServerInteractor.get()?.let { currentServer ->
            refreshSettingsInteractor.refreshAsync(currentServer)
            refreshPermissionsInteractor.refreshAsync(currentServer)
            connectionManagerFactory.create(currentServer).connect()
        }
    }

    fun getCurrentServerName() {
        getCurrentServerInteractor.get()?.let { currentServer ->
            mainView.setupToolbar(currentServer)
        }
    }

    fun getAllServers() {
        launchUI(cancelStrategy) {
            try {
                mainView.setupServerListView(getAccountsInteractor.get())
            } catch (exception: Exception) {
                Timber.e(exception, "Error while getting all servers")
                when (exception) {
                    is RocketChatAuthException -> logout()
                    else -> {
                        exception.message?.let {
                            mainView.showMessage(it)
                        }.ifNull {
                            mainView.showGenericErrorMessage()
                        }
                    }
                }
            }
        }
    }

    fun showChatList(chatRoomId: String? = null) = mainNavigator.toChatList(chatRoomId)

    fun showSettings() = mainNavigator.toSettings()

    fun logout() {
        getCurrentServerInteractor.get()?.let { currentServer ->
            setupConnectionInfo(currentServer)
        }
    }
}
