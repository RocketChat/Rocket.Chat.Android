package chat.rocket.android.createchannel.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.members.uimodel.MemberUiModelMapper
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.CURRENT_SERVER

class CreateChannelPresenterTest {

    private val view = mock(CreateChannelView::class.java)
    private val strategy = mock(CancelStrategy::class.java)
    private val mapper = mock(MemberUiModelMapper::class.java)
    private val navigator = mock(MainNavigator::class.java)
    private val serverInteractor = mock(GetCurrentServerInteractor::class.java)
    private val factory = mock(RocketChatClientFactory::class.java)

    private lateinit var createChannelPresenter: CreateChannelPresenter


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(serverInteractor.get()).thenReturn(CURRENT_SERVER)
        createChannelPresenter = CreateChannelPresenter(
            view, strategy, mapper, navigator, serverInteractor, factory
        )
    }

    @Test
    fun `navigate to chat list`() {
        createChannelPresenter.toChatList()
        verify(navigator).toChatList()
    }
}
