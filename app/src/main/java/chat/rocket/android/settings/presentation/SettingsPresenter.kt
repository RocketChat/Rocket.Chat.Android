package chat.rocket.android.settings.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import chat.rocket.android.R
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.dynamiclinks.DynamicLinksForFirebase
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.AnalyticsTrackingInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.domain.RemoveAccountInteractor
import chat.rocket.android.server.domain.SaveCurrentLanguageInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.server.presentation.CheckServerPresenter
import chat.rocket.android.util.extension.HashType
import chat.rocket.android.util.extension.hash
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.adminPanelUrl
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.deleteOwnAccount
import chat.rocket.core.internal.rest.serverInfo
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
    private val userHelper: UserHelper,
    private val analyticsTrackingInteractor: AnalyticsTrackingInteractor,
    private val tokenRepository: TokenRepository,
    private val permissions: PermissionsInteractor,
    private val rocketChatClientFactory: RocketChatClientFactory,
    private val dynamicLinksManager: DynamicLinksForFirebase,
    private val saveLanguageInteractor: SaveCurrentLanguageInteractor,
    getCurrentServerInteractor: GetCurrentServerInteractor,
    removeAccountInteractor: RemoveAccountInteractor,
    databaseManagerFactory: DatabaseManagerFactory?,
    connectionManagerFactory: ConnectionManagerFactory
) : CheckServerPresenter(
    strategy = strategy,
    factory = rocketChatClientFactory,
    currentSavedServer = currentServer,
    serverInteractor = getCurrentServerInteractor,
    removeAccountInteractor = removeAccountInteractor,
    tokenRepository = tokenRepository,
    dbManagerFactory = databaseManagerFactory,
    managerFactory = connectionManagerFactory,
    tokenView = view,
    navigator = navigator
) {
    private val token = currentServer?.let { tokenRepository.get(it) }

    fun setupView() {
        launchUI(strategy) {
            try {
                currentServer?.let {
                    val serverInfo = retryIO(description = "serverInfo", times = 5) {
                        rocketChatClientFactory.get(it).serverInfo()
                    }

                    userHelper.user()?.let { user ->
                        view.setupSettingsView(
                            it.avatarUrl(user.username!!, token?.userId, token?.authToken),
                            userHelper.displayName(user) ?: user.username ?: "",
                            user.status.toString(),
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
                currentServer?.let {
                    withContext(Dispatchers.Default) {
                        // REMARK: Backend API is only working with a lowercase hash.
                        // https://github.com/RocketChat/Rocket.Chat/issues/12573
                        retryIO {
                            rocketChatClientFactory.get(it)
                                .deleteOwnAccount(password.hash(HashType.Sha256).toLowerCase())
                        }
                        setupConnectionInfo(it)
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

    fun shareViaApp(context: Context?) {
        launchUI(strategy) {
            val user = userHelper.user()

            val deepLinkCallback = { returnedString: String? ->
                val link = returnedString ?: context?.getString(R.string.play_store_link)
                with(Intent(Intent.ACTION_SEND)) {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, context?.getString(R.string.msg_check_this_out))
                    putExtra(Intent.EXTRA_TEXT, link)
                    context?.startActivity(
                        Intent.createChooser(
                            this,
                            context.getString(R.string.msg_share_using)
                        )
                    )
                }
            }
            currentServer?.let {
                dynamicLinksManager.createDynamicLink(user?.username, it, deepLinkCallback)
            }
        }
    }

    fun recreateActivity() = navigator.recreateActivity()
}
