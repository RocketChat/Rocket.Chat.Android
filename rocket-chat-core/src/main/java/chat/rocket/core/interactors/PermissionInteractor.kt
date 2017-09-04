package chat.rocket.core.interactors

import chat.rocket.core.models.Permission
import chat.rocket.core.models.Room
import chat.rocket.core.models.RoomRole
import chat.rocket.core.repositories.*
import chat.rocket.core.utils.Pair
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import io.reactivex.functions.BiFunction

class PermissionInteractor(private val userRepository: UserRepository,
                           private val roomRoleRepository: RoomRoleRepository,
                           private val permissionRepository: PermissionRepository) {

    fun isAllowed(permissionId: String, room: Room): Single<Boolean> {
        return userRepository.getCurrent()
                .first(Optional.absent())
                .flatMap {
                    if (!it.isPresent) {
                        return@flatMap Single.just(false)
                    }

                    Single.zip<Optional<RoomRole>, Optional<Permission>, Pair<Optional<RoomRole>, Optional<Permission>>>(
                            roomRoleRepository.getFor(room, it.get()),
                            permissionRepository.getById(permissionId),
                            BiFunction { a, b -> Pair.create(a, b) }
                    )
                            .flatMap innerFlatMap@ {
                                if (!it.first.isPresent || !it.second.isPresent) {
                                    return@innerFlatMap Single.just(false)
                                }

                                val commonRoles = it.first.get().roles.intersect(
                                        it.second.get().roles
                                )

                                Single.just(commonRoles.isNotEmpty())
                            }
                }
    }
}
