package chat.rocket.android.server.domain

import javax.inject.Inject

class RemoveAccountInteractor @Inject constructor(val repository: AccountsRepository) {
    suspend fun remove(serverUrl: String) {
        repository.remove(serverUrl)
    }
}