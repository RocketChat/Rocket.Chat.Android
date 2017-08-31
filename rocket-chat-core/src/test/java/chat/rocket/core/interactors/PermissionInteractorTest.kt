package chat.rocket.core.interactors

import chat.rocket.core.models.*
import chat.rocket.core.repositories.PermissionRepository
import chat.rocket.core.repositories.RoomRoleRepository
import chat.rocket.core.repositories.UserRepository
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
class PermissionInteractorTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var roomRoleRepository: RoomRoleRepository

    @Mock
    lateinit var permissionRepository: PermissionRepository

    @Mock
    lateinit var room: Room

    @Mock
    lateinit var user: User

    @Mock
    lateinit var roomRole: RoomRole

    @Mock
    lateinit var permission: Permission

    lateinit var permissionInteractor: PermissionInteractor

    @Before
    fun setUp() {
        permissionInteractor = PermissionInteractor(
                userRepository, roomRoleRepository, permissionRepository
        )
    }

    @Test
    fun isAllowedReturnsFalseWhenWithoutCurrentUser() {

        `when`(userRepository.getCurrent())
                .thenReturn(Flowable.just(Optional.absent()))

        val testObserver = TestObserver<Boolean>()

        permissionInteractor.isAllowed("permission", room)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsFalseWhenWithoutRoomRoleAndPermission() {
        val permissionId = "permission"

        `when`(userRepository.getCurrent())
                .thenReturn(Flowable.just(Optional.of(user)))

        `when`(roomRoleRepository.getFor(any(Room::class.java), any(User::class.java)))
                .thenReturn(Single.just(Optional.absent()))

        `when`(permissionRepository.getById(permissionId))
                .thenReturn(Single.just(Optional.absent()))

        val testObserver = TestObserver<Boolean>()

        permissionInteractor.isAllowed(permissionId, room)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsFalseWhenWithoutRoomRole() {
        val permissionId = "permission"

        `when`(userRepository.getCurrent())
                .thenReturn(Flowable.just(Optional.of(user)))

        `when`(roomRoleRepository.getFor(any(Room::class.java), any(User::class.java)))
                .thenReturn(Single.just(Optional.absent()))

        `when`(permissionRepository.getById(permissionId))
                .thenReturn(Single.just(Optional.of(permission)))

        val testObserver = TestObserver<Boolean>()

        permissionInteractor.isAllowed(permissionId, room)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsFalseWhenWithoutPermission() {
        val permissionId = "permission"

        `when`(userRepository.getCurrent())
                .thenReturn(Flowable.just(Optional.of(user)))

        `when`(roomRoleRepository.getFor(any(Room::class.java), any(User::class.java)))
                .thenReturn(Single.just(Optional.of(roomRole)))

        `when`(permissionRepository.getById(permissionId))
                .thenReturn(Single.just(Optional.absent()))

        val testObserver = TestObserver<Boolean>()

        permissionInteractor.isAllowed(permissionId, room)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsFalseWhenRoomRoleAndPermissionDoesNotMatchWithEmptyRoles() {

        val permissionId = "permission"

        `when`(userRepository.getCurrent())
                .thenReturn(Flowable.just(Optional.of(user)))

        `when`(roomRoleRepository.getFor(any(Room::class.java), any(User::class.java)))
                .thenReturn(Single.just(Optional.of(roomRole)))

        `when`(permissionRepository.getById(permissionId))
                .thenReturn(Single.just(Optional.of(permission)))

        val testObserver = TestObserver<Boolean>()

        permissionInteractor.isAllowed(permissionId, room)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsFalseWhenRoomRoleAndPermissionDoesNotMatchWithRoles() {

        val permissionId = "permission"

        `when`(userRepository.getCurrent())
                .thenReturn(Flowable.just(Optional.of(user)))

        `when`(roomRole.roles).thenReturn(getSomeRoles())

        `when`(roomRoleRepository.getFor(any(Room::class.java), any(User::class.java)))
                .thenReturn(Single.just(Optional.of(roomRole)))

        `when`(permission.roles).thenReturn(getOtherRoles())

        `when`(permissionRepository.getById(permissionId))
                .thenReturn(Single.just(Optional.of(permission)))

        val testObserver = TestObserver<Boolean>()

        permissionInteractor.isAllowed(permissionId, room)
                .subscribe(testObserver)

        testObserver.assertResult(false)
    }

    @Test
    fun isAllowedReturnsTrueWhenRoomRoleAndPermissionDoesMatch() {

        val permissionId = "permission"

        `when`(userRepository.getCurrent())
                .thenReturn(Flowable.just(Optional.of(user)))

        `when`(roomRole.roles).thenReturn(getMoreRoles())

        `when`(roomRoleRepository.getFor(any(Room::class.java), any(User::class.java)))
                .thenReturn(Single.just(Optional.of(roomRole)))

        `when`(permission.roles).thenReturn(getOtherRoles())

        `when`(permissionRepository.getById(permissionId))
                .thenReturn(Single.just(Optional.of(permission)))

        val testObserver = TestObserver<Boolean>()

        permissionInteractor.isAllowed(permissionId, room)
                .subscribe(testObserver)

        testObserver.assertResult(true)
    }

    private fun getSomeRoles() = listOf(
            Role.builder().setId("one role id").setName("one role name").build()
    )

    private fun getOtherRoles() = listOf(
            Role.builder().setId("other role id").setName("other role name").build(),
            Role.builder().setId("another role id").setName("another role name").build()
    )

    private fun getMoreRoles() = getSomeRoles() + listOf(
            Role.builder().setId("other role id").setName("other role name").build(),
            Role.builder().setId("another role id").setName("another role name").build()
    )

}