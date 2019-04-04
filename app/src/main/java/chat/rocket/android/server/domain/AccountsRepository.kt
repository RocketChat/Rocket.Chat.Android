package chat.rocket.android.server.domain

import chat.rocket.android.server.domain.model.Account

interface AccountsRepository {

    fun save(account: Account)
    fun load(): List<Account>
    fun remove(serverUrl: String)
}