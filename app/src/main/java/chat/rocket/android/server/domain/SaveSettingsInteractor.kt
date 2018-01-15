package chat.rocket.android.server.domain

import chat.rocket.core.model.Value
import javax.inject.Inject

class SaveSettingsInteractor @Inject constructor(private val repository: SettingsRepository) {

    fun save(url: String, settings: Map<String, Value<Any>>) = repository.save(url, settings)
}