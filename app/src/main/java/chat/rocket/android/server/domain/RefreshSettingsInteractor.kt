package chat.rocket.android.server.domain

import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.core.internal.rest.settings
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

class RefreshSettingsInteractor @Inject constructor(private val factory: RocketChatClientFactory,
                                                    private val repository: SettingsRepository) {

    private var settingsFilter = arrayOf(
        LDAP_ENABLE, CAS_ENABLE, CAS_LOGIN_URL,

        ACCOUNT_REGISTRATION, ACCOUNT_LOGIN_FORM, ACCOUNT_PASSWORD_RESET, ACCOUNT_CUSTOM_FIELDS,
        ACCOUNT_GOOGLE, ACCOUNT_FACEBOOK, ACCOUNT_GITHUB, ACCOUNT_LINKEDIN, ACCOUNT_METEOR,
        ACCOUNT_TWITTER, ACCOUNT_WORDPRESS, ACCOUNT_GITLAB,

        SITE_URL, SITE_NAME, FAVICON_512, FAVICON_196, USE_REALNAME, ALLOW_ROOM_NAME_SPECIAL_CHARS,
        FAVORITE_ROOMS, UPLOAD_STORAGE_TYPE, UPLOAD_MAX_FILE_SIZE, UPLOAD_WHITELIST_MIMETYPES,
        HIDE_USER_JOIN, HIDE_USER_LEAVE,
        HIDE_TYPE_AU, HIDE_MUTE_UNMUTE, HIDE_TYPE_RU, ALLOW_MESSAGE_DELETING,
        ALLOW_MESSAGE_EDITING, ALLOW_MESSAGE_PINNING, SHOW_DELETED_STATUS, SHOW_EDITED_STATUS,
        WIDE_TILE_310)

    suspend fun refresh(server: String) {
        withContext(CommonPool) {
            factory.create(server).let { client ->
                val settings = client.settings(*settingsFilter)
                repository.save(server, settings)
            }
        }
    }

    fun refreshAsync(server: String) {
        async {
            try {
                refresh(server)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}