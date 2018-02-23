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
        SITE_URL, SITE_NAME, FAVICON_512, USE_REALNAME, ALLOW_ROOM_NAME_SPECIAL_CHARS,
        FAVORITE_ROOMS, ACCOUNT_LOGIN_FORM, ACCOUNT_GOOGLE, ACCOUNT_FACEBOOK, ACCOUNT_GITHUB,
        ACCOUNT_GITLAB, ACCOUNT_LINKEDIN, ACCOUNT_METEOR, ACCOUNT_TWITTER, ACCOUNT_WORDPRESS,
        LDAP_ENABLE, ACCOUNT_REGISTRATION, UPLOAD_STORAGE_TYPE, UPLOAD_MAX_FILE_SIZE,
        UPLOAD_WHITELIST_MIMETYPES, HIDE_USER_JOIN, HIDE_USER_LEAVE, HIDE_TYPE_AU, HIDE_MUTE_UNMUTE,
        HIDE_TYPE_RU, ACCOUNT_CUSTOM_FIELDS, ALLOW_MESSAGE_DELETING, ALLOW_MESSAGE_EDITING,
        ALLOW_MESSAGE_PINNING, SHOW_DELETED_STATUS, SHOW_EDITED_STATUS)

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