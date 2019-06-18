package chat.rocket.android.authentication.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.PRIVACY_POLICY
import testConfig.Config.Companion.TERMS_OF_SERVICE
import testConfig.Config.Companion.currentServer
import testConfig.Config.Companion.privacyPolicyUrl
import testConfig.Config.Companion.termsOfServiceUrl


class AuthenticationPresenterTest {

    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val navigator = Mockito.mock(AuthenticationNavigator::class.java)
    private val tokenRepository = Mockito.mock(TokenRepository::class.java)
    private val localRepository = Mockito.mock(LocalRepository::class.java)
    private val settingsRepository = Mockito.mock(SettingsRepository::class.java)
    private val getCurrentServer = Mockito.mock(GetCurrentServerInteractor::class.java)
    private val getAccountInteractor = Mockito.mock(GetAccountInteractor::class.java)
    private val serverInteractor = Mockito.mock(GetConnectingServerInteractor::class.java)

    private lateinit var authenticationPresenter: AuthenticationPresenter


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(serverInteractor.get()).thenReturn(currentServer)
        authenticationPresenter = AuthenticationPresenter(
            strategy, navigator, getCurrentServer, getAccountInteractor, settingsRepository,
            localRepository, tokenRepository, serverInteractor
        )
    }

    @Test
    fun check_terms_of_service() {
        authenticationPresenter.termsOfService(TERMS_OF_SERVICE)
        verify(navigator).toWebPage(termsOfServiceUrl, TERMS_OF_SERVICE)
    }

    @Test
    fun check_privacy_policy() {
        authenticationPresenter.privacyPolicy(PRIVACY_POLICY)
        verify(navigator).toWebPage(privacyPolicyUrl, PRIVACY_POLICY)
    }

    @Test
    fun check_navigator_to_chatlist() {
        authenticationPresenter.toChatList()
        verify(navigator).toChatList()
    }
}