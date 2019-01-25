package chat.rocket.android.server.domain

import javax.inject.Inject

class SaveClientCertInteractor @Inject constructor(private val repository: ClientCertRepository) {
    fun save(alias: String) = repository.save(alias)
}
