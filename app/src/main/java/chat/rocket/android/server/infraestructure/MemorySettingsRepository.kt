package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.core.model.Value

class MemorySettingsRepository : SettingsRepository {

    val cache = HashMap<String, Map<String, Value<Any>>>()

    override fun save(url: String, settings: Map<String, Value<Any>>) {
        cache.put(url, settings)
    }

    override fun get(url: String): Map<String, Value<Any>>? {
        return cache[url]
    }

}