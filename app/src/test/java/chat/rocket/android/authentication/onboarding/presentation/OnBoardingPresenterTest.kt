package chat.rocket.android.authentication.onboarding.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.SaveConnectingServerInteractor
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.communityServerUrl
import testConfig.Config.Companion.currentServer


class OnBoardingPresenterTest {

    lateinit var onBoardingPresenter: OnBoardingPresenter

    private val view = Mockito.mock(OnBoardingView::class.java)
    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val navigator = Mockito.mock(AuthenticationNavigator::class.java)
    private val serverInteractor = Mockito.mock(SaveConnectingServerInteractor::class.java)
    private val refreshSettingsInteractor = Mockito.mock(RefreshSettingsInteractor::class.java)
    private val getAccountsInteractor = Mockito.mock(GetAccountsInteractor::class.java)
    private val settingsInteractor = Mockito.mock(GetSettingsInteractor::class.java)
    private val factory = Mockito.mock(RocketChatClientFactory::class.java)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        onBoardingPresenter = OnBoardingPresenter(
            view, strategy, navigator, serverInteractor, refreshSettingsInteractor,
            getAccountsInteractor, settingsInteractor, factory, currentServer
        )
    }

    @Test
    fun `navigate to sign in to your server`() {
        onBoardingPresenter.toSignInToYourServer()
        verify(navigator).toSignInToYourServer()
    }

    @Test
    fun `navigate to web page if new server is created`() {
        onBoardingPresenter.toCreateANewServer(communityServerUrl)
        verify(navigator).toWebPage(communityServerUrl)
    }
}