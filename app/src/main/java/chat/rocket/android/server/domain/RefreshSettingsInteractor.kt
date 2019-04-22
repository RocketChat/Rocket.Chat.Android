package chat.rocket.android.server.domain

import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.retryIO
import chat.rocket.core.internal.rest.settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * This class reloads the current logged server settings whenever needed.
 */
class RefreshSettingsInteractor @Inject constructor(
    private val factory: RocketChatClientFactory,
    private val repository: SettingsRepository
) {

    private var settingsFilter = arrayOf(
        UNIQUE_IDENTIFIER,

        LDAP_ENABLE,
        CAS_ENABLE,
        CAS_LOGIN_URL,
        ACCOUNT_REGISTRATION,
        ACCOUNT_LOGIN_FORM,
        ACCOUNT_PASSWORD_RESET,
        ACCOUNT_CUSTOM_FIELDS,
        ACCOUNT_GOOGLE,
        ACCOUNT_FACEBOOK,
        ACCOUNT_GITHUB,
        ACCOUNT_LINKEDIN,
        ACCOUNT_METEOR,
        ACCOUNT_TWITTER,
        ACCOUNT_GITLAB,
        ACCOUNT_GITLAB_URL,
        ACCOUNT_WORDPRESS,
        ACCOUNT_WORDPRESS_URL,

        JITSI_ENABLED,
        JISTI_ENABLE_CHANNELS,
        JITSI_SSL,
        JITSI_DOMAIN,
        JITSI_URL_ROOM_PREFIX,

        SITE_URL,
        SITE_NAME,
        FAVICON_512,
        FAVICON_196,
        USE_REALNAME,
        ALLOW_ROOM_NAME_SPECIAL_CHARS,
        FAVORITE_ROOMS,
        UPLOAD_STORAGE_TYPE,
        UPLOAD_MAX_FILE_SIZE,
        UPLOAD_WHITELIST_MIMETYPES,
        HIDE_USER_JOIN,
        HIDE_USER_LEAVE,
        HIDE_TYPE_AU,
        HIDE_MUTE_UNMUTE,
        HIDE_TYPE_RU,
        ALLOW_MESSAGE_DELETING,
        ALLOW_MESSAGE_EDITING,
        ALLOW_MESSAGE_PINNING,
        ALLOW_MESSAGE_STARRING,
        SHOW_DELETED_STATUS,
        SHOW_EDITED_STATUS,
        WIDE_TILE_310,
        STORE_LAST_MESSAGE,
        MESSAGE_READ_RECEIPT_ENABLED,
        MESSAGE_READ_RECEIPT_STORE_USERS
    )

    suspend fun refresh(server: String) {
        withContext(Dispatchers.IO) {
            factory.create(server).let { client ->
                val settings = retryIO(
                    description = "settings",
                    times = 5,
                    maxDelay = 5000,
                    initialDelay = 300
                ) {
                    client.settings(*settingsFilter)
                }
                repository.save(server, settings)
            }
        }
    }

    fun refreshAsync(server: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                refresh(server)
            } catch (ex: Exception) {
                Timber.e(ex, "Error refreshing settings for: $server")
            }
        }
    }
}