package chat.rocket.android.main.presentation

import chat.rocket.android.authentication.domain.model.DeepLinkInfo
import chat.rocket.android.core.behaviours.AppLanguageView
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.push.retrieveCurrentPushNotificationToken
import chat.rocket.android.server.domain.GetCurrentLanguageInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.RefreshPermissionsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.RemoveAccountInteractor
import chat.rocket.android.server.domain.SaveAccountInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.favicon
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.domain.siteName
import chat.rocket.android.server.domain.wideTile
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.serverLogoUrl
import javax.inject.Inject
import javax.inject.Named

class MainPresenter @Inject constructor(
    @Named("currentServer") private val currentServer: String?,
    private val strategy: CancelStrategy,
    private val mainNavigator: MainNavigator,
    private val appLanguageView: AppLanguageView,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val refreshPermissionsInteractor: RefreshPermissionsInteractor,
    private val getSettingsInteractor: GetSettingsInteractor,
    private val connectionManagerFactory: ConnectionManagerFactory,
    private var getLanguageInteractor: GetCurrentLanguageInteractor,
    private val groupedPush: GroupedPush,
    private val tokenRepository: TokenRepository,
    private val userHelper: UserHelper,
    private val saveAccountInteractor: SaveAccountInteractor,
    private val removeAccountInteractor: RemoveAccountInteractor,
    private val factory: RocketChatClientFactory
) {

    fun connect() = currentServer?.let {
        refreshSettingsInteractor.refreshAsync(it)
        refreshPermissionsInteractor.refreshAsync(it)
        connectionManagerFactory.create(it)?.connect()
    }

    fun clearNotificationsForChatRoom(chatRoomId: String?) {
        if (chatRoomId == null) return
        groupedPush.hostToPushMessageList[currentServer].let { list ->
            list?.removeAll { it.info.roomId == chatRoomId }
        }
    }

    fun getAppLanguage() {
        with(getLanguageInteractor) {
            getLanguage()?.let { language ->
                appLanguageView.updateLanguage(language, getCountry())
            }
        }
    }

    fun removeOldAccount() = currentServer?.let {
        removeAccountInteractor.remove(currentServer)
    }

    fun saveNewAccount() {
        currentServer?.let { currentServer ->
            with(getSettingsInteractor.get(currentServer)) {
                val icon = favicon()?.let {
                    currentServer.serverLogoUrl(it)
                }
                val logo = wideTile()?.let {
                    currentServer.serverLogoUrl(it)
                }
                val token = tokenRepository.get(currentServer)
                val thumb = currentServer.avatarUrl(
                    userHelper.username() ?: "",
                    token?.userId,
                    token?.authToken
                )

                val account = Account(
                    serverName = siteName() ?: currentServer,
                    serverUrl = currentServer,
                    serverLogoUrl = icon,
                    serverBackgroundImageUrl = logo,
                    userName = userHelper.username() ?: "",
                    userAvatarUrl = thumb,
                    authToken = token?.authToken,
                    userId = token?.userId
                )

                saveAccountInteractor.save(account)
            }
        }
    }

    fun registerPushNotificationToken() = launchUI(strategy) {
        currentServer?.let { retrieveCurrentPushNotificationToken(factory.get(it)) }
    }

    fun showChatList(chatRoomId: String? = null, deepLinkInfo: DeepLinkInfo? = null) =
        mainNavigator.toChatList(chatRoomId, deepLinkInfo)
}
