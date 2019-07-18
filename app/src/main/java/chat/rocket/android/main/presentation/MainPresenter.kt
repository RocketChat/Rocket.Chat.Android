package chat.rocket.android.main.presentation

import chat.rocket.android.authentication.domain.model.DeepLinkInfo
import chat.rocket.android.core.behaviours.AppLanguageView
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.server.domain.GetCurrentLanguageInteractor
import chat.rocket.android.server.domain.RefreshPermissionsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import javax.inject.Inject
import javax.inject.Named

class MainPresenter @Inject constructor(
    @Named("currentServer") private val currentServer: String?,
    private val mainNavigator: MainNavigator,
    private val appLanguageView: AppLanguageView,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val refreshPermissionsInteractor: RefreshPermissionsInteractor,
    private val connectionManagerFactory: ConnectionManagerFactory,
    private var getLanguageInteractor: GetCurrentLanguageInteractor,
    private val groupedPush: GroupedPush
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

    fun showChatList(chatRoomId: String? = null, deepLinkInfo: DeepLinkInfo? = null) =
        mainNavigator.toChatList(chatRoomId, deepLinkInfo)

    fun getAppLanguage() {
        with(getLanguageInteractor) {
            getLanguage()?.let { language ->
                appLanguageView.updateLanguage(language, getCountry())
            }
        }
    }
}
