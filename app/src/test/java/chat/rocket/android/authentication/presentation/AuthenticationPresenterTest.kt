package chat.rocket.android.authentication.presentation

import chat.rocket.android.authentication.domain.model.DeepLinkInfo
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.PRIVACY_POLICY
import testConfig.Config.Companion.TERMS_OF_SERVICE
import testConfig.Config.Companion.currentServer
import testConfig.Config.Companion.privacyPolicyUrl
import testConfig.Config.Companion.termsOfServiceUrl


class AuthenticationPresenterTest {

    private lateinit var authenticationPresenter: AuthenticationPresenter

    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val navigator = Mockito.mock(AuthenticationNavigator::class.java)
    private val tokenRepository = Mockito.mock(TokenRepository::class.java)
    private val localRepository = Mockito.mock(LocalRepository::class.java)
    private val settingsRepository = Mockito.mock(SettingsRepository::class.java)
    private val getCurrentServer = Mockito.mock(GetCurrentServerInteractor::class.java)
    private val getAccountInteractor = Mockito.mock(GetAccountInteractor::class.java)
    private val serverInteractor = Mockito.mock(GetConnectingServerInteractor::class.java)

    private val deepLinkInfo = DeepLinkInfo(
        "www.abc.com", "UserId", "token",
        "rId", "public", "abc"
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(strategy.isTest).thenReturn(true)
        `when`(serverInteractor.get()).thenReturn(currentServer)
        authenticationPresenter = AuthenticationPresenter(
            strategy, navigator, getCurrentServer, getAccountInteractor, settingsRepository,
            localRepository, tokenRepository, serverInteractor
        )
    }

    @Test
    fun `navigate to tos web page`() {
        authenticationPresenter.termsOfService(TERMS_OF_SERVICE)
        verify(navigator).toWebPage(termsOfServiceUrl, TERMS_OF_SERVICE)
    }

    @Test
    fun `navigate to privacy policy web page`() {
        authenticationPresenter.privacyPolicy(PRIVACY_POLICY)
        verify(navigator).toWebPage(privacyPolicyUrl, PRIVACY_POLICY)
    }

    @Test
    fun `navigate to Onboarding`() {
        authenticationPresenter.toOnBoarding()
        verify(navigator).toOnBoarding()
    }

    @Test
    fun `navigate to sign in to your server`() {
        authenticationPresenter.toSignInToYourServer(deepLinkInfo)
        verify(navigator).toSignInToYourServer(deepLinkInfo, false)
    }

    @Test
    fun `save deep link info`() {
        authenticationPresenter.saveDeepLinkInfo(deepLinkInfo)
        verify(navigator).saveDeepLinkInfo(deepLinkInfo)
    }

    @Test
    fun `navigate to chat list`() {
        authenticationPresenter.toChatList()
        verify(navigator).toChatList()
    }

    @Test
    fun `navigate to chat list with deeplink`() {
        authenticationPresenter.toChatList(deepLinkInfo)
        verify(navigator).toChatList(deepLinkInfo)
    }
}