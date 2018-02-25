package chat.rocket.android.server.infraestructure

import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.LocalRepository.Companion.SETTINGS_KEY
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.core.internal.SettingsAdapter
import chat.rocket.core.model.Value

class SharedPreferencesSettingsRepository(private val localRepository: LocalRepository) : SettingsRepository {

    private val adapter = SettingsAdapter().lenient()

    override fun save(url: String, settings: Map<String, Value<Any>>) {
        localRepository.save("$SETTINGS_KEY$url", adapter.toJson(settings))
    }

    override fun get(url: String): Map<String, Value<Any>>? {
        val settings = localRepository.get("$SETTINGS_KEY$url")
        settings?.let {
            return adapter.fromJson(it)
        }

        return null
    }
}