package chat.rocket.core.interactors

import chat.rocket.core.SortDirection
import chat.rocket.core.models.Message
import chat.rocket.core.models.Room
import chat.rocket.core.models.SpotlightUser
import chat.rocket.core.models.User
import chat.rocket.core.repositories.MessageRepository
import chat.rocket.core.repositories.SpotlightUserRepository
import chat.rocket.core.repositories.UserRepository
import chat.rocket.core.temp.TempSpotlightUserCaller
import chat.rocket.core.utils.Triple
import io.reactivex.Flowable
import io.reactivex.functions.Function3
import java.util.ArrayList

class AutocompleteUserInteractor(private val room: Room,
                                 private val userRepository: UserRepository,
                                 private val messageRepository: MessageRepository,
                                 private val spotlightUserRepository: SpotlightUserRepository,
                                 private val tempSpotlightUserCaller: TempSpotlightUserCaller) {

    private val groupMentions = listOf(
            SpotlightUser.builder().setId("all").setUsername("all").setStatus("online").build(),
            SpotlightUser.builder().setId("here").setUsername("here").setStatus("online").build()
    )

    fun getSuggestionsFor(name: String): Flowable<List<SpotlightUser>> {
        return Flowable.zip<String, List<Message>, List<SpotlightUser>, Triple<String, List<Message>, List<SpotlightUser>>>(
                Flowable.just(name),
                messageRepository.getAllFrom(room),
                userRepository.getSortedLikeName(name, 5).map { it.toSpotlightUsers() },
                Function3 { a, b, c -> Triple.create(a, b, c) }
        )
                .flatMap { triple ->
                    val recentUsers = triple.second.takeUsers(5).toSpotlightUsers()

                    if (triple.first.isEmpty()) {
                        return@flatMap Flowable.just(recentUsers + groupMentions)
                    }

                    val workedUsers = (recentUsers.filter { it.username.contains(triple.first, true) } + triple.third).distinct().take(5)
                    if (workedUsers.size == 5) {
                        return@flatMap Flowable.just(workedUsers + groupMentions.filter { it.username.contains(triple.first, true) })
                    }

                    tempSpotlightUserCaller.search(triple.first)

                    spotlightUserRepository.getSuggestionsFor(triple.first, SortDirection.DESC, 5)
                            .withLatestFrom<List<SpotlightUser>, List<SpotlightUser>, Triple<List<SpotlightUser>, List<SpotlightUser>, List<SpotlightUser>>>(
                                    Flowable.just(workedUsers),
                                    Flowable.just(groupMentions.filter { it.username.contains(triple.first, true) }),
                                    Function3 { a, b, c -> Triple.create(a, b, c) }
                            )
                            .map { triple ->
                                val spotlightUsers = triple.first + triple.second

                                return@map spotlightUsers.distinct().take(5) + triple.third
                            }
                }
    }
}

fun List<User>.toSpotlightUsers(): List<SpotlightUser> {
    val size = this.size
    val spotlightUsers = ArrayList<SpotlightUser>(size)

    for (i in 0..size - 1) {
        val user = this[i]
        spotlightUsers.add(SpotlightUser.builder()
                .setId(user.id)
                .setUsername(user.username)
                .setStatus(user.status)
                .build())
    }

    return spotlightUsers
}

fun List<Message>.takeUsers(n: Int): List<User> {
    val users = ArrayList<User>()

    this.forEach {
        if (it.user != null && !users.contains(it.user!!)) {
            users.add(it.user!!)
            if (users.size == n) {
                return@forEach
            }
        }
    }

    return users
}
