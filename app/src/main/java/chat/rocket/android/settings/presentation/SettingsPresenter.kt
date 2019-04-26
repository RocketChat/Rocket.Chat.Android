package chat.rocket.android.settings.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.AnalyticsTrackingInteractor
import chat.rocket.android.server.domain.GetCurrentLanguageInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.domain.RemoveAccountInteractor
import chat.rocket.android.server.domain.SaveCurrentLanguageInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.server.presentation.CheckServerPresenter
import chat.rocket.android.util.extension.gethash
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extension.toHex
import chat.rocket.android.util.extensions.adminPanelUrl
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.deleteOwnAccount
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.serverInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class SettingsPresenter @Inject constructor(
    private val view: SettingsView,
    private val strategy: CancelStrategy,
    private val navigator: MainNavigator,
    @Named("currentServer") private val currentServer: String,
    private val userHelper: UserHelper,
    private val analyticsTrackingInteractor: AnalyticsTrackingInteractor,
    private val tokenRepository: TokenRepository,
    private val permissions: PermissionsInteractor,
    private val rocketChatClientFactory: RocketChatClientFactory,
    getCurrentServerInteractor: GetCurrentServerInteractor,
    removeAccountInteractor: RemoveAccountInteractor,
    databaseManagerFactory: DatabaseManagerFactory,
    connectionManagerFactory: ConnectionManagerFactory,
    private val saveLanguageInteractor: SaveCurrentLanguageInteractor
) : CheckServerPresenter(
    strategy = strategy,
    factory = rocketChatClientFactory,
    serverInteractor = getCurrentServerInteractor,
    removeAccountInteractor = removeAccountInteractor,
    tokenRepository = tokenRepository,
    dbManagerFactory = databaseManagerFactory,
    managerFactory = connectionManagerFactory,
    tokenView = view,
    navigator = navigator
) {

    fun setupView() {
        launchUI(strategy) {
            try {
                val serverInfo = retryIO(description = "serverInfo", times = 5) {
                    rocketChatClientFactory.get(currentServer).serverInfo()
                }

                val me = retryIO(description = "serverInfo", times = 5) {
                    rocketChatClientFactory.get(currentServer).me()
                }

                userHelper.user()?.let { user ->
                    view.setupSettingsView(
                        currentServer.avatarUrl(me.username ?: ""),
                        userHelper.displayName(user) ?: me.username ?: "",
                        me.status.toString(),
                        permissions.isAdministrationEnabled(),
                        analyticsTrackingInteractor.get(),
                        true,
                        serverInfo.version
                    )
                }
            } catch (exception: Exception) {
                Timber.d(exception, "Error getting server info")
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
        }
    }

    fun enableAnalyticsTracking(isEnabled: Boolean) {
        analyticsTrackingInteractor.save(isEnabled)

    }

    fun logout() {
        setupConnectionInfo(currentServer)
        super.logout(null) // TODO null?
    }

    fun deleteAccount(password: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                withContext(Dispatchers.Default) {
                    // REMARK: Backend API is only working with a lowercase hash.
                    // https://github.com/RocketChat/Rocket.Chat/issues/12573
                    retryIO {
                        rocketChatClientFactory.get(currentServer)
                            .deleteOwnAccount(password.gethash().toHex().toLowerCase())
                    }
                    setupConnectionInfo(currentServer)
                    logout(null)
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

    fun saveLocale(language: String, country: String? = null) {
        saveLanguageInteractor.save(language, country)
    }

    fun toProfile() = navigator.toProfile()

    fun toAdmin() = tokenRepository.get(currentServer)?.let {
        navigator.toAdminPanel(currentServer.adminPanelUrl(), it.authToken)
    }

    fun toLicense(licenseUrl: String, licenseTitle: String) =
        navigator.toLicense(licenseUrl, licenseTitle)
}