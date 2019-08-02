package chat.rocket.android.authentication.resetpassword.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetConnectingServerInteractor
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.EMAIL
import testConfig.Config.Companion.currentServer

class ResetPasswordPresenterTest {

    private val view = mock(ResetPasswordView::class.java)
    private val strategy = mock(CancelStrategy::class.java)
    private val navigator = mock(AuthenticationNavigator::class.java)
    private val factory = mock(RocketChatClientFactory::class.java)
    private val serverInteractor = mock(GetConnectingServerInteractor::class.java)

    private lateinit var resetPasswordPresenter: ResetPasswordPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(strategy.isTest).thenReturn(true)
        `when`(serverInteractor.get()).thenReturn(currentServer)
        resetPasswordPresenter = ResetPasswordPresenter(
            view, strategy, navigator, factory, serverInteractor
        )
    }

    @Test
    fun `reset password`() {
        kotlinx.coroutines.runBlocking {
            val result = resetPasswordPresenter.resetPassword(EMAIL)
            assertEquals(result, Unit)
        }
    }
}