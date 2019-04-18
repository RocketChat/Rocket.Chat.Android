package chat.rocket.android.server.domain

import chat.rocket.android.server.domain.model.Account
import javax.inject.Inject

class SaveAccountInteractor @Inject constructor(val repository: AccountsRepository) {
    fun save(account: Account) = repository.save(account)
}