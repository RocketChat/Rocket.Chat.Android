package chat.rocket.android.server.domain

import chat.rocket.android.server.domain.model.Account

interface AccountsRepository {
    suspend fun save(account: Account)
    suspend fun load(): List<Account>
    suspend fun remove(serverUrl: String)
}