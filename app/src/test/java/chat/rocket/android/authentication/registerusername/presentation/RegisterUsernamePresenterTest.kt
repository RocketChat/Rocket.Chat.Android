package chat.rocket.android.authentication.registerusername.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.CURRENT_SERVER
import testConfig.Config.Companion.USER_AVATAR
import testConfig.Config.Companion.USER_NAME

class RegisterUsernamePresenterTest {

    private val view = mock(RegisterUsernameView::class.java)
    private val strategy = mock(CancelStrategy::class.java)
    private val navigator = mock(AuthenticationNavigator::class.java)
    private val tokenRepository = mock(TokenRepository::class.java)
    private val settingsInteractor = mock(GetSettingsInteractor::class.java)
    private val analyticsManager = mock(AnalyticsManager::class.java)
    private val saveCurrentServer = mock(SaveCurrentServerInteractor::class.java)
    private val saveAccountInteractor = mock(SaveAccountInteractor::class.java)
    private val factory = mock(RocketChatClientFactory::class.java)
    private val serverInteractor = mock(GetConnectingServerInteractor::class.java)

    private lateinit var registerUsernamePresenter: RegisterUsernamePresenter

    private val account = Account(
        CURRENT_SERVER, CURRENT_SERVER, null, null,
        USER_NAME, USER_AVATAR, null, null
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(serverInteractor.get()).thenReturn(CURRENT_SERVER)
        registerUsernamePresenter = RegisterUsernamePresenter(
            view, strategy, navigator, tokenRepository, saveAccountInteractor, analyticsManager,
            saveCurrentServer, serverInteractor, factory, settingsInteractor
        )
    }

    @Test
    fun `save new user account`() {
        registerUsernamePresenter.saveAccount(USER_NAME)
        verify(saveAccountInteractor).save(account)
    }
}