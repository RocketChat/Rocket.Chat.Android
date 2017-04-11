package chat.rocket.core.interactors

import chat.rocket.core.models.SpotlightUser
import chat.rocket.core.models.User
import chat.rocket.core.repositories.SpotlightUserRepository
import chat.rocket.core.repositories.UserRepository
import chat.rocket.core.temp.TempSpotlightUserCaller
import chat.rocket.core.utils.Triple
import io.reactivex.Flowable
import java.util.ArrayList

class AutocompleteUserInteractor(private val userRepository: UserRepository,
                                 private val spotlightUserRepository: SpotlightUserRepository,
                                 private val tempSpotlightUserCaller: TempSpotlightUserCaller) {

    fun getSuggestionsFor(name: String): Flowable<List<SpotlightUser>> {
        return Flowable.zip<String, List<SpotlightUser>, List<SpotlightUser>, Triple<String, List<SpotlightUser>, List<SpotlightUser>>>(
                Flowable.just(name),
                userRepository.getSortedLikeName(name)
        )
    }

    private fun toSpotlightRooms(users: List<User>): List<SpotlightUser> {
        val size = users.size
        val spotlightUsers = ArrayList<SpotlightUser>(size)

        for (i in 0..size - 1) {
            val user = users[i]
            spotlightUsers.add(SpotlightUser.builder()
                    .setId(user.id)
                    .setUsername(user.username)
                    .setStatus(user.status)
                    .build())
        }

        return spotlightUsers
    }
}
