package chat.rocket.android.server.domain

import javax.inject.Inject

class GetAccountsInteractor @Inject constructor(val repository: AccountsRepository) {
    fun get() = repository.load()
}