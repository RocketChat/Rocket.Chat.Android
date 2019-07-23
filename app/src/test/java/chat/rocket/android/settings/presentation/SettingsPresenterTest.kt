package chat.rocket.android.settings.presentation

import android.content.Context
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.dynamiclinks.DynamicLinksForFirebase
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import org.junit.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.ADMIN_PANEL_URL
import testConfig.Config.Companion.LICENSE
import testConfig.Config.Companion.LICENSE_URL
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.currentServer


class SettingsPresenterTest {

    private val view = Mockito.mock(SettingsView::class.java)
    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val navigator = Mockito.mock(MainNavigator::class.java)
    private val userHelper = Mockito.mock(UserHelper::class.java)
    private val analyticsTrackingInteractor = Mockito.mock(AnalyticsTrackingInteractor::class.java)
    private val tokenRepository = Mockito.mock(TokenRepository::class.java)
    private val permissions = Mockito.mock(PermissionsInteractor::class.java)
    private val rocketChatClientFactory = Mockito.mock(RocketChatClientFactory::class.java)
    private val dynamicLinksManager = Mockito.mock(DynamicLinksForFirebase::class.java)
    private val saveLanguageInteractor = Mockito.mock(SaveCurrentLanguageInteractor::class.java)
    private val getCurrentServerInteractor = Mockito.mock(GetCurrentServerInteractor::class.java)
    private val removeAccountInteractor = Mockito.mock(RemoveAccountInteractor::class.java)
    private val databaseManagerFactory = Mockito.mock(DatabaseManagerFactory::class.java)
    private val connectionManagerFactory = Mockito.mock(ConnectionManagerFactory::class.java)
    private val serverInteractor = Mockito.mock(GetConnectingServerInteractor::class.java)
    private val mockApplicationContext = Mockito.mock(Context::class.java)

    lateinit var settingsPresenter: SettingsPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(serverInteractor.get()).thenReturn(currentServer)
        Mockito.`when`(strategy.isTest).thenReturn(true)
        settingsPresenter = SettingsPresenter(
            view, strategy, navigator, currentServer, userHelper, analyticsTrackingInteractor,
            tokenRepository, permissions, rocketChatClientFactory, dynamicLinksManager, saveLanguageInteractor,
            getCurrentServerInteractor, removeAccountInteractor, databaseManagerFactory, connectionManagerFactory
        )
    }

    @Test
    fun setupView()  {
        val result = settingsPresenter.setupView()
        assertEquals(result, Unit)
    }

    @Test
    fun deleteAccount() = runBlocking {
        val result = settingsPresenter.deleteAccount(PASSWORD)
        assertEquals(result ,Unit)
    }

    @Test
    fun shareViaApp() = runBlocking  {
        val result = settingsPresenter.shareViaApp(mockApplicationContext)
        assertEquals(result ,Unit)
    }

    @Test
    fun TrackingShouldBeEnable() {
        settingsPresenter.enableAnalyticsTracking(true)
        verify(analyticsTrackingInteractor).save(true)
    }

    @Test
    fun TrackingShouldBeDisable() {
        settingsPresenter.enableAnalyticsTracking(false)
        verify(analyticsTrackingInteractor).save(false)
    }

    @Test
    fun saveLocaleWithCountry() {
        settingsPresenter.saveLocale("hi", "rIN")
        verify(saveLanguageInteractor).save("hi", "rIN")
    }

    @Test
    fun saveLocaleWithNoCountry() {
        settingsPresenter.saveLocale("hi", null)
        verify(saveLanguageInteractor).save("hi", null)
    }

    @Test
    fun navigateToProfile() {
        settingsPresenter.toProfile()
        verify(navigator).toProfile()
    }

    @Test
    fun navigateToAdmin() {
        settingsPresenter.toAdmin()
        val a = tokenRepository.get(currentServer)
        a?.authToken?.let { verify(navigator).toAdminPanel(ADMIN_PANEL_URL, it) }
    }

    @Test
    fun navigateToLicense() {
        settingsPresenter.toLicense(LICENSE_URL, LICENSE)
        verify(navigator).toLicense(LICENSE_URL, LICENSE)
    }

    @Test
    fun activityShouldBeRecreated() {
        settingsPresenter.recreateActivity()
        verify(navigator).recreateActivity()
    }
}