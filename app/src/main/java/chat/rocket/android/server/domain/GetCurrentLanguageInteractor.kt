package chat.rocket.android.server.domain

import chat.rocket.android.server.infrastructure.CurrentLanguageRepository
import javax.inject.Inject

class GetCurrentLanguageInteractor @Inject constructor(
    private val repository: CurrentLanguageRepository
) {

    fun getLanguage(): String? = repository.getLanguage()
    fun getCountry(): String? = repository.getCountry()
}