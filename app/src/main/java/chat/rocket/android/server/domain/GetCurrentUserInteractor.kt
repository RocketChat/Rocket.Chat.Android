package chat.rocket.android.server.domain

import chat.rocket.android.db.UserDao
import chat.rocket.android.db.model.UserEntity

class GetCurrentUserInteractor(
    private val tokenRepository: TokenRepository,
    private val currentServer: String,
    private val userDao: UserDao
) {
    fun get(): UserEntity? {
        return tokenRepository.get(currentServer)?.let {
            userDao.getUser(it.userId)
        }
    }

}
