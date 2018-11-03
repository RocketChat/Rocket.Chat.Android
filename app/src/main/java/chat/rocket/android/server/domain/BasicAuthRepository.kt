package chat.rocket.android.server.domain

import chat.rocket.android.server.domain.model.BasicAuth

interface BasicAuthRepository {
    fun save(basicAuth: BasicAuth)
    fun load(): List<BasicAuth>
}
