package chat.rocket.android.infrastructure

import chat.rocket.common.model.User

interface LocalRepository {

    fun save(key: String, value: String?)
    fun save(key: String, value: Boolean)
    fun save(key: String, value: Int)
    fun save(key: String, value: Long)
    fun save(key: String, value: Float)
    fun get(key: String, defValue: String? = null): String?
    fun getBoolean(key: String, defValue: Boolean = false): Boolean
    fun getFloat(key: String, defValue: Float = -1f): Float
    fun getInt(key: String, defValue: Int = -1): Int
    fun getLong(key: String, defValue: Long = -1L): Long
    fun clear(key: String)
    fun clearAllFromServer(server: String)
    fun getCurrentUser(url: String): User?
    fun saveCurrentUser(url: String, user: User)
    fun saveLastChatroomsRefresh(url: String, timestamp: Long)
    fun getLastChatroomsRefresh(url: String): Long

    companion object {
        const val KEY_PUSH_TOKEN = "KEY_PUSH_TOKEN"
        const val TOKEN_KEY = "token_"
        const val SETTINGS_KEY = "settings_"
        const val PERMISSIONS_KEY = "permissions_"
        const val USER_KEY = "user_"
        const val DRAFT_KEY = "draft"
        const val CURRENT_USERNAME_KEY = "username_"
        const val LAST_CHATROOMS_REFRESH = "_chatrooms_refresh"
    }
}

// FIXME - we are saving the user full name here when the server is UI_Use_Real_Name true
fun LocalRepository.checkIfMyself(username: String) = username() == username
fun LocalRepository.username() = get(LocalRepository.CURRENT_USERNAME_KEY)