package chat.rocket.android.members.presentation

import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.members.uimodel.MemberUiModelMapper
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import testConfig.Config.Companion.CURRENT_SERVER

class MembersPresenterTest {

    private val view = Mockito.mock(MembersView::class.java)
    private val navigator = Mockito.mock(ChatRoomNavigator::class.java)
    private val dbManager = Mockito.mock(DatabaseManager::class.java)
    private val permissionsInteractor = Mockito.mock(PermissionsInteractor::class.java)
    private val strategy = Mockito.mock(CancelStrategy::class.java)
    private val mapper = Mockito.mock(MemberUiModelMapper::class.java)
    private val userHelper = Mockito.mock(UserHelper::class.java)
    private val factory = Mockito.mock(RocketChatClientFactory::class.java)

    private lateinit var membersPresenter: MembersPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(strategy.isTest).thenReturn(true)
        membersPresenter = MembersPresenter(
            view, navigator, dbManager, permissionsInteractor,
            CURRENT_SERVER, strategy, mapper, factory, userHelper
        )
    }

    @Test
    fun `load chat room members`() {
        val result = membersPresenter.loadChatRoomsMembers("123")
        assertEquals(result, Unit)
    }

    @Test
    fun `navigate to invite user`() {
        membersPresenter.toInviteUsers("123")
        verify(navigator).toInviteUsers("123")
    }
}