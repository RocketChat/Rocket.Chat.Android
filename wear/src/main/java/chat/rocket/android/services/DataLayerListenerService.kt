package chat.rocket.android.services

import chat.rocket.android.server.*
import chat.rocket.android.util.AppPreferenceManager
import chat.rocket.android.util.Constants.KEY_PREFS_ACTIVITY_FOREGROUND
import chat.rocket.android.util.TokenSerialisableModel
import chat.rocket.android.util.deserialiseToken
import chat.rocket.android.util.retryIO
import chat.rocket.common.model.Token
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.android.AndroidInjection
import kotlinx.coroutines.experimental.launch
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
                val loginToken = Token(tokenToSave.getFirst(), tokenToSave.getSecond())
                tokenRepository.save(currentServer, loginToken)

                client = factory.create(currentServer)
                launch {
                    username = retryIO("me()") { client.me().username }
                    if (username != null) {
                        localRepository.save(LocalRepository.CURRENT_USERNAME_KEY, username)
                    }

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
            getCurrentServerInteractor.clear()
        }
    }
}