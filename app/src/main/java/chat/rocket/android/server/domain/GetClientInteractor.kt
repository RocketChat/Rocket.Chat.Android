package chat.rocket.android.server.domain

import javax.inject.Inject

class GetClientCertInteractor @Inject constructor(private val repository: ClientCertRepository) {
    fun get(): String? = repository.get()

    fun clear() {
        repository.clear()
    }
}
