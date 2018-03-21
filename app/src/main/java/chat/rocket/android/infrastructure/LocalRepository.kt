package chat.rocket.android.infrastructure

interface LocalRepository {

    companion object {
        const val KEY_PUSH_TOKEN = "KEY_PUSH_TOKEN"
        const val TOKEN_KEY = "token_"
        const val SETTINGS_KEY = "settings_"
        const val USERNAME_KEY = "my_username"
        const val UNFINISHED_MSG_KEY = "unfinished_msg_"
    }

    fun save(key: String, value: String?)

    fun get(key: String): String?

    fun clear(key: String)

    fun clearAllFromServer(server: String)
}