package chat.rocket.android.authentication.infraestructure

import chat.rocket.common.model.Token
import chat.rocket.core.TokenRepository

class MemoryTokenRepository : TokenRepository {
    var savedToken: Token? = null

    override fun get(): Token? {
        return savedToken
    }

    override fun save(token: Token) {
        savedToken = token
    }
}