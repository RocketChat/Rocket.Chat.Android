package chat.rocket.android.server.domain

import javax.inject.Inject

class RemoveAccountInterector @Inject constructor(val repository: AccountsRepository) {
    suspend fun remove(serverUrl: String) {
        repository.remove(serverUrl)
    }
}