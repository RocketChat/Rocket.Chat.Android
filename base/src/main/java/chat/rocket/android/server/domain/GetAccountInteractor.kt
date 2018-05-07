package chat.rocket.android.server.domain

import javax.inject.Inject

class GetAccountInteractor @Inject constructor(val repository: AccountsRepository) {
    suspend fun get(url: String) = repository.load().firstOrNull { account ->
        url == account.serverUrl
    }
}