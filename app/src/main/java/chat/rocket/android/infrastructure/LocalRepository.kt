package chat.rocket.android.infrastructure

interface LocalRepository {

    fun save(key: String, value: String?)
    fun save(key: String, value: Boolean)
    fun save(key: String, value: Int)
    fun save(key: String, value: Long)
    fun save(key: String, value: Float)
    fun get(key: String): String?
    fun getBoolean(key: String): Boolean
    fun getFloat(key: String): Float
    fun getInt(key: String): Int
    fun getLong(key: String): Long
    fun clear(key: String)
    fun clearAllFromServer(server: String)

    companion object {
        const val KEY_PUSH_TOKEN = "KEY_PUSH_TOKEN"
        const val MIGRATION_FINISHED_KEY = "MIGRATION_FINISHED_KEY"
        const val TOKEN_KEY = "token_"
        const val SETTINGS_KEY = "settings_"
        const val CURRENT_USERNAME_KEY = "username_"
    }
}

fun LocalRepository.checkIfMyself(username: String) = get(LocalRepository.CURRENT_USERNAME_KEY) == username