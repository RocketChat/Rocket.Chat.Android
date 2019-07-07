package chat.rocket.android.authentication.signup.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.currentServer
import testConfig.Config.Companion.privacyPolicyUrl
import testConfig.Config.Companion.termsOfServiceUrl


class SignupPresenterTest {

    private val view = mock(SignupView::class.java)
    private val strategy = mock(CancelStrategy::class.java)
    private val navigator = mock(AuthenticationNavigator::class.java)
    private val localRepository = mock(LocalRepository::class.java)
    private val settingsInteractor = mock(GetSettingsInteractor::class.java)
    private val analyticsManager = mock(AnalyticsManager::class.java)
    private val saveCurrentServerInteractor = mock(SaveCurrentServerInteractor::class.java)
    private val saveAccountInteractor = mock(SaveAccountInteractor::class.java)
    private val factory = mock(RocketChatClientFactory::class.java)
    private val serverInteractor = mock(GetConnectingServerInteractor::class.java)
    private val tokenRepository = mock(TokenRepository::class.java)

    lateinit var signUpPresenter: SignupPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(serverInteractor.get()).thenReturn(currentServer)
        signUpPresenter = SignupPresenter(view, strategy, navigator, localRepository, serverInteractor, saveCurrentServerInteractor,
            analyticsManager, factory, saveAccountInteractor, tokenRepository, settingsInteractor)
    }

    @Test
    fun `navigate to tos web page`() {
        signUpPresenter.termsOfService()
        verify(navigator).toWebPage(termsOfServiceUrl)
    }

    @Test
    fun `navigate to privacy policy web page`() {
        signUpPresenter.privacyPolicy()
        verify(navigator).toWebPage(privacyPolicyUrl)
    }
}