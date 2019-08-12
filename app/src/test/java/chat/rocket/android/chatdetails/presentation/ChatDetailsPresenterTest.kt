package chat.rocket.android.chatdetails.presentation

import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infrastructure.ConnectionManagerFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.CHAT_ROOM_ID
import testConfig.Config.Companion.CHAT_ROOM_TYPE
import testConfig.Config.Companion.CURRENT_SERVER


class ChatDetailsPresenterTest {

    private val view = Mockito.mock(ChatDetailsView::class.java)
    private val navigator = Mockito.mock(ChatRoomNavigator::class.java)
    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val serverInteractor = Mockito.mock(GetCurrentServerInteractor::class.java)
    private val factory = Mockito.mock(ConnectionManagerFactory::class.java)

    private lateinit var chatDetailsPresenter: ChatDetailsPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(serverInteractor.get()).thenReturn(CURRENT_SERVER)
        chatDetailsPresenter = ChatDetailsPresenter(
            view, navigator, strategy, serverInteractor, factory
        )
    }

    @Test
    fun `naviagate to video conference`() {
        chatDetailsPresenter.toVideoConference(CHAT_ROOM_ID, CHAT_ROOM_TYPE)
        verify(navigator).toVideoConference(CHAT_ROOM_ID, CHAT_ROOM_TYPE)
    }

    @Test
    fun `naviagate to files`() {
        chatDetailsPresenter.toFiles(CHAT_ROOM_ID)
        verify(navigator).toFileList(CHAT_ROOM_ID)
    }

    @Test
    fun `naviagate to member`() {
        chatDetailsPresenter.toMembers(CHAT_ROOM_ID)
        verify(navigator).toMembersList(CHAT_ROOM_ID)
    }

    @Test
    fun `naviagate to mention`() {
        chatDetailsPresenter.toMentions(CHAT_ROOM_ID)
        verify(navigator).toMentions(CHAT_ROOM_ID)
    }

    @Test
    fun `naviagate to pinned`() {
        chatDetailsPresenter.toPinned(CHAT_ROOM_ID)
        verify(navigator).toPinnedMessageList(CHAT_ROOM_ID)
    }

    @Test
    fun `naviagate to favorites`() {
        chatDetailsPresenter.toFavorites(CHAT_ROOM_ID)
        verify(navigator).toFavoriteMessageList(CHAT_ROOM_ID)
    }
}