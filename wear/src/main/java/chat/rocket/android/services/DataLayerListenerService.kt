package chat.rocket.android.services

import chat.rocket.android.server.*
import chat.rocket.android.util.AppPreferenceManager
import chat.rocket.android.util.Constants.KEY_PREFS_ACTIVITY_FOREGROUND
import chat.rocket.android.util.TokenSerialisableModel
import chat.rocket.android.util.deserialiseToken
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.Token
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.logout
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.registerPushToken
import chat.rocket.core.internal.rest.unregisterPushToken
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import dagger.android.AndroidInjection
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

internal const val PATH_TOKEN = "/send-token"
internal const val PATH_SERVER = "/send-server"
internal const val PATH_DELETE = "/delete-server"

class DataLayerListenerService : WearableListenerService() {

    @Inject
    lateinit var getCurrentServerInteractor: GetCurrentServerInteractor
    @Inject
    lateinit var serverInteractor: SaveCurrentServerInteractor
    @Inject
    lateinit var tokenRepository: TokenRepository
    @Inject
    lateinit var factory: RocketChatClientFactory
    @Inject
    lateinit var localRepository: LocalRepository

    private lateinit var client: RocketChatClient
    private var username: String? = null

    private lateinit var sharedPreferencesManager: AppPreferenceManager
    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        sharedPreferencesManager = AppPreferenceManager(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        if (path == PATH_TOKEN) {
            //save token request
            val tokenToSave: TokenSerialisableModel = deserialiseToken(messageEvent.data)
            val currentServer = getCurrentServerInteractor.get()
            if (currentServer != null) {
                val loginToken = Token(tokenToSave.getTokenUserId(), tokenToSave.getAuthToken())
                tokenRepository.save(currentServer, loginToken)

                client = factory.create(currentServer)
                launch {
                    username = retryIO("me()") { client.me().username }
                    if (username != null) {
                        localRepository.save(LocalRepository.CURRENT_USERNAME_KEY, username)
                    }

                    registerPushToken(currentServer)

                    val isActivityForeground =
                        sharedPreferencesManager.getSharedPreferenceBoolean(
                            KEY_PREFS_ACTIVITY_FOREGROUND
                        )
                    if (isActivityForeground) {
                        //inform main activity about saving tokens if it is in foreground
                        //to lead the user to the main activity
                    }
                }
            }
        } else if (path == PATH_SERVER) {
            //save server request
            val currentServerUrl = String(messageEvent.data, Charsets.UTF_8)
            serverInteractor.save(currentServerUrl)
        } else if (path == PATH_DELETE) {
            //delete tokens and server
            logout()
        }
    }

    private suspend fun registerPushToken(serverUrl: String) {
        launch {
            localRepository.get(LocalRepository.KEY_PUSH_TOKEN)?.let {
                val client = factory.create(serverUrl)
                try {
                    retryIO(description = "register push token for $serverUrl") {
                        client.registerPushToken(it)
                    }
                } catch (ex: Exception) {
                    Timber.d(ex, "Error registering Push token for $serverUrl")
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun logout() {
        val currentServer = getCurrentServerInteractor.get()
        currentServer?.let {
            launch {
                try {
                    clearTokens(currentServer)
                    retryIO("logout") { client.logout() }
                } catch (exception: RocketChatException) {
                    Timber.d(exception, "Error calling logout")
                }
                try {
                    tokenRepository.remove(currentServer)
                } catch (ex: Exception) {
                    Timber.d(ex, "Error cleaning up the session...")
                }
            }
        }
    }

    private suspend fun clearTokens(currentServer: String) {
        getCurrentServerInteractor.clear()
        val pushToken = localRepository.get(LocalRepository.KEY_PUSH_TOKEN)
        if (pushToken != null) {
            try {
                retryIO("unregisterPushToken") { client.unregisterPushToken(pushToken) }
                FirebaseInstanceId.getInstance()
                    .deleteToken(pushToken, FirebaseMessaging.INSTANCE_ID_SCOPE)
            } catch (ex: Exception) {
                Timber.d(ex, "Error unregistering push token")
            }
        }
        localRepository.clearAllFromServer(currentServer)
    }
}