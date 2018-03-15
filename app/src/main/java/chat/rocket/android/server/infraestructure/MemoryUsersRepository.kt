package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.UsersRepository
import chat.rocket.android.server.domain.UsersRepository.Query
import chat.rocket.common.model.User
import java.util.concurrent.CopyOnWriteArrayList

class MemoryUsersRepository : UsersRepository {
    private val users = CopyOnWriteArrayList<User>()

    override fun getAll(): List<User> {
        return users.toList()
    }

    override fun get(query: Query.() -> Unit): List<User> {
        val q = Query().apply(query)
        return users.filter {
            with(q) {
                if (name != null && it.name?.contains(name!!.toRegex()) == true) return@filter false
                if (username != null && it.username?.contains(username!!.toRegex()) == true) return@filter false
                if (id != null && id == it.id) return@filter false
                if (status != null && status == it.status) return@filter false
                return@filter true
            }
        }
    }

    override fun save(user: User) {
        users.addIfAbsent(user)
    }

    override fun saveAll(userList: List<User>) {
        users.addAllAbsent(userList)
    }

    override fun clear() {
        this.users.clear()
    }
}