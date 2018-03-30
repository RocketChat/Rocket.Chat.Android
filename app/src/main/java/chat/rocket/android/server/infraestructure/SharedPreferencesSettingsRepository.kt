package chat.rocket.android.server.infraestructure

import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.LocalRepository.Companion.SETTINGS_KEY
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.core.internal.SettingsAdapter

class SharedPreferencesSettingsRepository(private val localRepository: LocalRepository) : SettingsRepository {

    private val adapter = SettingsAdapter().lenient()

    override fun save(url: String, settings: PublicSettings) {
        localRepository.save("$SETTINGS_KEY$url", adapter.toJson(settings))
    }

    override fun get(url: String): PublicSettings {
        val settings = localRepository.get("$SETTINGS_KEY$url")
        return if (settings == null) hashMapOf() else adapter.fromJson(settings) ?: hashMapOf()
    }
}