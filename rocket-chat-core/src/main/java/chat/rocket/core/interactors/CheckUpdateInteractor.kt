package chat.rocket.core.interactors

import chat.rocket.core.repositories.UpdateRepository
import io.reactivex.Observable

class CheckUpdateInteractor(private val repository: UpdateRepository) {

    fun check(): Observable<Boolean> = repository.getUpdateAvailable().doOnSubscribe { repository.refresh() }

}
