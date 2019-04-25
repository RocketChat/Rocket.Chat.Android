package chat.rocket.android.server.domain

import chat.rocket.android.server.infraestructure.CurrentLanguageRepository
import javax.inject.Inject

class GetCurrentLanguageInteractor @Inject constructor(
    private val repository: CurrentLanguageRepository
) {

    fun get(): String? = repository.get()
}