package chat.rocket.android.main.presentation

import android.content.Context
import android.content.res.Configuration
import chat.rocket.android.push.GroupedPush
import chat.rocket.android.server.domain.GetCurrentLanguageInteractor
import chat.rocket.android.server.domain.RefreshPermissionsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.SaveCurrentLanguageInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class MainPresenter @Inject constructor(
    @Named("currentServer") private val currentServerUrl: String,
    private val mainNavigator: MainNavigator,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val refreshPermissionsInteractor: RefreshPermissionsInteractor,
    private val connectionManagerFactory: ConnectionManagerFactory,
    private val saveLanguageInteractor: SaveCurrentLanguageInteractor,
    private var getLanguageInteractor: GetCurrentLanguageInteractor,
    private val groupedPush: GroupedPush
) {

    fun connect() {
        refreshSettingsInteractor.refreshAsync(currentServerUrl)
        refreshPermissionsInteractor.refreshAsync(currentServerUrl)
        connectionManagerFactory.create(currentServerUrl).connect()
    }

    fun clearNotificationsForChatRoom(chatRoomId: String?) {
        if (chatRoomId == null) return

        groupedPush.hostToPushMessageList[currentServerUrl].let { list ->
            list?.removeAll { it.info.roomId == chatRoomId }
        }
    }

    fun showChatList(chatRoomId: String? = null) = mainNavigator.toChatList(chatRoomId)

    fun setLocale(language: String, baseContext: Context) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        saveLanguageInteractor.save(language)
    }

    fun setLocaleWithRegion(lang: String, country: String, baseContext: Context) {
        val locale = Locale(lang, country)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        saveLanguageInteractor.save(lang)
    }

    fun loadLocale(baseContext: Context) {
        val currentLanguage = getLanguageInteractor.get()
        if (currentLanguage != null) {
            setLocale(currentLanguage, baseContext)
        }
    }
}