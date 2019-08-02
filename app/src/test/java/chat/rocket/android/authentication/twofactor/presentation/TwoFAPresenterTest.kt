package chat.rocket.android.authentication.twofactor.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.AUTHENTICATION_CODE
import testConfig.Config.Companion.EMAIL
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.currentServer


class TwoFAPresenterTest {

    private val view = mock(TwoFAView::class.java)
    private val strategy = mock(CancelStrategy::class.java)
    private val navigator = mock(AuthenticationNavigator::class.java)
    private val tokenRepository = mock(TokenRepository::class.java)
    private val localRepository = mock(LocalRepository::class.java)
    private val saveCurrentServer = mock(SaveCurrentServerInteractor::class.java)
    private val analyticsManager = mock(AnalyticsManager::class.java)
    private val factory = mock(RocketChatClientFactory::class.java)
    private val saveAccountInteractor = mock(SaveAccountInteractor::class.java)
    private val serverInteractor = mock(GetConnectingServerInteractor::class.java)
    private val settingsInteractor = mock(GetSettingsInteractor::class.java)

    private lateinit var twoFAPresenter: TwoFAPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(strategy.isTest).thenReturn(true)
        `when`(serverInteractor.get()).thenReturn(currentServer)
        twoFAPresenter = TwoFAPresenter(
            view, strategy, navigator, tokenRepository, localRepository, saveCurrentServer,
            analyticsManager, factory, saveAccountInteractor, serverInteractor, settingsInteractor
        )
    }

    @Test
    fun `successful authentication`() {
        kotlinx.coroutines.runBlocking {
            val result = twoFAPresenter.authenticate(EMAIL, PASSWORD, AUTHENTICATION_CODE)
            assertEquals(result, Unit)
        }
    }
}