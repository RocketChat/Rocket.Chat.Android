package chat.rocket.android.infrastructure

interface LocalRepository {

    companion object {
        val KEY_PUSH_TOKEN = "KEY_PUSH_TOKEN"
    }

    fun save(key: String, value: String?)

    fun get(key: String): String?
}