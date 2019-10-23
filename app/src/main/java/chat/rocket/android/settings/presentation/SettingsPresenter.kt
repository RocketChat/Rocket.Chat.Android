package chat.rocket.android.settings.presentation

import android.content.Context
import android.os.Build
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.dynamiclinks.DynamicLinksForFirebase
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.push.retrieveCurrentPushNotificationToken
import chat.rocket.android.server.domain.AnalyticsTrackingInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.domain.RemoveAccountInteractor
import chat.rocket.android.server.domain.SaveCurrentLanguageInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.util.extension.HashType
import chat.rocket.android.util.extension.hash
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.adminPanelUrl
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.invalidateFirebaseToken
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.deleteOwnAccount
import chat.rocket.core.internal.rest.logout
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.serverInfo
import chat.rocket.core.model.Myself
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class SettingsPresenter @Inject constructor(
    private val view: SettingsView,
    private val strategy: CancelStrategy,
    private val navigator: MainNavigator,
    @Named("currentServer") private val currentServer: String?,
    private val analyticsTrackingInteractor: AnalyticsTrackingInteractor,
    private val tokenRepository: TokenRepository,
    private val permissions: PermissionsInteractor,
    private val rocketChatClientFactory: RocketChatClientFactory,
    private val dynamicLinksManager: DynamicLinksForFirebase,
    private val saveLanguageInteractor: SaveCurrentLanguageInteractor,
    private val serverInteractor: GetCurrentServerInteractor,
    private val localRepository: LocalRepository,
    private val connectionManagerFactory: ConnectionManagerFactory,
    private val removeAccountInteractor: RemoveAccountInteractor,
    private val dbManagerFactory: DatabaseManagerFactory
) {
    private val token = currentServer?.let { tokenRepository.get(it) }
    private lateinit var me: Myself

    fun setupView() {
        launchUI(strategy) {
            try {
                view.showLoading()
                currentServer?.let { serverUrl ->
                    val serverInfo = retryIO(description = "serverInfo", times = 5) {
                        rocketChatClientFactory.get(serverUrl).serverInfo()
                    }

                    me = retryIO(description = "me", times = 5) {
                        rocketChatClientFactory.get(serverUrl).me()
                    }

                    me.username?.let { username ->
                        view.setupSettingsView(
                            serverUrl.avatarUrl(username, token?.userId, token?.authToken),
                            username,
                            me.status.toString(),
                            permissions.isAdministrationEnabled(),
                            analyticsTrackingInteractor.get(),
                            true,
                            serverInfo.version
                        )
                    }
                }
            } catch (exception: Exception) {
                Timber.d(exception, "Error getting server info")
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

    fun enableAnalyticsTracking(isEnabled: Boolean) {
        analyticsTrackingInteractor.save(isEnabled)
    }

    fun deleteAccount(password: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                currentServer?.let { currentServer ->
                    withContext(Dispatchers.Default) {
                        // REMARK: Backend API is only working with a lowercase hash.
                        // https://github.com/RocketChat/Rocket.Chat/issues/12573
                        rocketChatClientFactory.get(currentServer)
                            .deleteOwnAccount(password.hash(HashType.Sha256).toLowerCase())
                        logout()
                    }
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

    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }
    }

    fun saveLocale(language: String, country: String? = null) {
        saveLanguageInteractor.save(language, country)
    }

    fun toProfile() = navigator.toProfile()

    fun toAdmin() = currentServer?.let { currentServer ->
        tokenRepository.get(currentServer)?.let {
            navigator.toAdminPanel(currentServer.adminPanelUrl(), it.authToken)
        }
    }

    fun toLicense(licenseUrl: String, licenseTitle: String) =
        navigator.toLicense(licenseUrl, licenseTitle)

    fun prepareShareApp() {
        launchUI(strategy) {
            val deepLinkCallback = { returnedString: String? ->
                view.openShareApp(returnedString)
            }

            currentServer?.let { currentServer ->
                me.username?.let { username ->
                    dynamicLinksManager.createDynamicLink(username, currentServer, deepLinkCallback)
                }
            }
        }
    }

    fun recreateActivity() = navigator.recreateActivity()

    /**
     * Logout the user from the current server.
     */
    fun logout() {
        launchUI(strategy) {
            try {
                currentServer?.let { currentServer ->
                    rocketChatClientFactory.get(currentServer).let { client ->
                        retrieveCurrentPushNotificationToken(client, true)
                        tokenRepository.remove(currentServer)

                        serverInteractor.clear()
                        localRepository.clearAllFromServer(currentServer)
                        removeAccountInteractor.remove(currentServer)

                        withContext(Dispatchers.IO) {
                            invalidateFirebaseToken()
                            dbManagerFactory.create(currentServer)?.logout()
                        }
                        connectionManagerFactory.create(currentServer)?.disconnect()
                        client.logout()
                        navigator.switchOrAddNewServer()
                    }
                }
            } catch (exception: RocketChatException) {
                Timber.e(exception, "Error while trying to logout")
            }
        }
    }
}
