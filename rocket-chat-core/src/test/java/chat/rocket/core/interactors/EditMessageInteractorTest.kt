package chat.rocket.core.interactors

import chat.rocket.core.PermissionsConstants
import chat.rocket.core.PublicSettingsConstants
import chat.rocket.core.models.Message
import chat.rocket.core.models.PublicSetting
import chat.rocket.core.models.Room
import chat.rocket.core.models.User
import chat.rocket.core.repositories.*
import com.hadisatrio.optional.Optional
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.observers.TestObserver

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EditMessageInteractorTest {

    @Mock
    lateinit var permissionInteractor: PermissionInteractor

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var messageRepository: MessageRepository

    @Mock
    lateinit var roomRepository: RoomRepository

    @Mock
    lateinit var publicSettingRepository: PublicSettingRepository

    @Mock
    lateinit var message: Message

    @Mock
    lateinit var user: User

    @Mock
    lateinit var room: Room

    lateinit var editMessageInteractor: EditMessageInteractor

    @Before
    fun setUp() {
        editMessageInteractor = EditMessageInteractor(
                permissionInteractor, userRepository, messageRepository, roomRepository, publicSettingRepository
        )
    }

    @Test
    fun isAllowedReturnsFalseWhenWithoutRoom() {
        val testObserver = TestObserver<Boolean>()

        `when`(user.id).thenReturn("id")

        `when`(message.roomId).thenReturn("roomId")
        `when`(message.user).thenReturn(user)

        val allowEdit = allowEditPublicSettings(true)
        val allowEditTimeout = allowEditTimeLimitPublicSetting()

        `when`(userRepository.getCurrent()).thenReturn(Flowable.just(Optional.of(user)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING))
                .thenReturn(Single.just(Optional.of(allowEdit)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING_BLOCK_TIMEOUT))
                .thenReturn(Single.just(Optional.of(allowEditTimeout)))

        `when`(roomRepository.getById(anyString())).thenReturn(Flowable.just(Optional.absent()))

        editMessageInteractor.isAllowed(message)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsFalseWhenEditNotAllowed() {
        val testObserver = TestObserver<Boolean>()

        `when`(user.id).thenReturn("id")

        `when`(message.roomId).thenReturn("roomId")
        `when`(message.user).thenReturn(user)

        val allowEdit = allowEditPublicSettings(false)
        val allowEditTimeout = allowEditTimeLimitPublicSetting()

        `when`(userRepository.getCurrent()).thenReturn(Flowable.just(Optional.of(user)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING))
                .thenReturn(Single.just(Optional.of(allowEdit)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING_BLOCK_TIMEOUT))
                .thenReturn(Single.just(Optional.of(allowEditTimeout)))

        `when`(roomRepository.getById(anyString())).thenReturn(Flowable.just(Optional.of(room)))

        `when`(permissionInteractor.isAllowed(PermissionsConstants.EDIT_MESSAGE, room))
                .thenReturn(Single.just(false))

        editMessageInteractor.isAllowed(message)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsFalseWhenEditTimeout() {
        val testObserver = TestObserver<Boolean>()

        `when`(user.id).thenReturn("id")

        `when`(message.roomId).thenReturn("roomId")
        `when`(message.timestamp).thenReturn(120_000)
        `when`(message.user).thenReturn(user)

        val allowEdit = allowEditPublicSettings(true)
        val allowEditTimeout = allowEditTimeLimitPublicSetting(1)

        `when`(userRepository.getCurrent()).thenReturn(Flowable.just(Optional.of(user)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING))
                .thenReturn(Single.just(Optional.of(allowEdit)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING_BLOCK_TIMEOUT))
                .thenReturn(Single.just(Optional.of(allowEditTimeout)))

        `when`(roomRepository.getById(anyString())).thenReturn(Flowable.just(Optional.of(room)))

        `when`(permissionInteractor.isAllowed(PermissionsConstants.EDIT_MESSAGE, room))
                .thenReturn(Single.just(false))

        editMessageInteractor.isAllowed(message)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsFalseWhenNotTheSameUser() {
        val testObserver = TestObserver<Boolean>()

        `when`(user.id).thenReturn("id")

        `when`(message.roomId).thenReturn("roomId")
        `when`(message.user).thenReturn(user)

        val allowEdit = allowEditPublicSettings(true)
        val allowEditTimeout = allowEditTimeLimitPublicSetting()

        val anotherUser = mock(User::class.java)
        `when`(anotherUser.id).thenReturn("another id")

        `when`(userRepository.getCurrent()).thenReturn(Flowable.just(Optional.of(anotherUser)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING))
                .thenReturn(Single.just(Optional.of(allowEdit)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING_BLOCK_TIMEOUT))
                .thenReturn(Single.just(Optional.of(allowEditTimeout)))

        `when`(roomRepository.getById(anyString())).thenReturn(Flowable.just(Optional.of(room)))

        `when`(permissionInteractor.isAllowed(PermissionsConstants.EDIT_MESSAGE, room))
                .thenReturn(Single.just(false))

        editMessageInteractor.isAllowed(message)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsTrueWhenEverythingIsOkAndNoPermission() {
        val testObserver = TestObserver<Boolean>()

        `when`(user.id).thenReturn("id")

        `when`(message.roomId).thenReturn("roomId")
        `when`(message.user).thenReturn(user)

        val allowEdit = allowEditPublicSettings(true)
        val allowEditTimeout = allowEditTimeLimitPublicSetting()

        `when`(userRepository.getCurrent()).thenReturn(Flowable.just(Optional.of(user)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING))
                .thenReturn(Single.just(Optional.of(allowEdit)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING_BLOCK_TIMEOUT))
                .thenReturn(Single.just(Optional.of(allowEditTimeout)))

        `when`(roomRepository.getById(anyString())).thenReturn(Flowable.just(Optional.of(room)))

        `when`(permissionInteractor.isAllowed(PermissionsConstants.EDIT_MESSAGE, room))
                .thenReturn(Single.just(false))

        editMessageInteractor.isAllowed(message)
                .subscribe(testObserver)

        testObserver.assertResult(true)
    }

    @Test
    fun isAllowedReturnsTrueWhenEverythingIsOkAndPermission() {
        val testObserver = TestObserver<Boolean>()

        `when`(user.id).thenReturn("id")

        `when`(message.roomId).thenReturn("roomId")
        `when`(message.user).thenReturn(user)

        val allowEdit = allowEditPublicSettings(true)
        val allowEditTimeout = allowEditTimeLimitPublicSetting()

        `when`(userRepository.getCurrent()).thenReturn(Flowable.just(Optional.of(user)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING))
                .thenReturn(Single.just(Optional.of(allowEdit)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING_BLOCK_TIMEOUT))
                .thenReturn(Single.just(Optional.of(allowEditTimeout)))

        `when`(roomRepository.getById(anyString())).thenReturn(Flowable.just(Optional.of(room)))

        `when`(permissionInteractor.isAllowed(PermissionsConstants.EDIT_MESSAGE, room))
                .thenReturn(Single.just(true))

        editMessageInteractor.isAllowed(message)
                .subscribe(testObserver)

        testObserver.assertResult(true)
    }

    @Test
    fun isAllowedReturnsTrueWhenEverythingIsNotOkAndPermission() {
        val testObserver = TestObserver<Boolean>()

        `when`(user.id).thenReturn("id")

        `when`(message.roomId).thenReturn("roomId")
        `when`(message.user).thenReturn(user)

        val allowEdit = allowEditPublicSettings(false)
        val allowEditTimeout = allowEditTimeLimitPublicSetting()

        `when`(userRepository.getCurrent()).thenReturn(Flowable.just(Optional.of(user)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING))
                .thenReturn(Single.just(Optional.of(allowEdit)))
        `when`(publicSettingRepository.getById(PublicSettingsConstants.Message.ALLOW_EDITING_BLOCK_TIMEOUT))
                .thenReturn(Single.just(Optional.of(allowEditTimeout)))

        `when`(roomRepository.getById(anyString())).thenReturn(Flowable.just(Optional.of(room)))

        `when`(permissionInteractor.isAllowed(PermissionsConstants.EDIT_MESSAGE, room))
                .thenReturn(Single.just(true))

        editMessageInteractor.isAllowed(message)
                .subscribe(testObserver)

        testObserver.assertResult(true)
    }

    private fun allowEditPublicSettings(isAllowed: Boolean = false) = mock(PublicSetting::class.java).apply {
        `when`(this.valueAsBoolean).thenReturn(isAllowed)
    }

    private fun allowEditTimeLimitPublicSetting(timeLimit: Long = 0) = mock(PublicSetting::class.java).apply {
        `when`(this.valueAsLong).thenReturn(timeLimit)
    }

}