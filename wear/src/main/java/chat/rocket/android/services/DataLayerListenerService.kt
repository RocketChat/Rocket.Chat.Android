package chat.rocket.android.services

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.SaveCurrentServerInteractor
import chat.rocket.android.server.TokenRepository
import chat.rocket.android.util.AppPreferenceManager
import chat.rocket.android.util.Constants.KEY_PREFS_ACTIVITY_FOREGROUND
import chat.rocket.android.util.TokenSerialisableModel
import chat.rocket.android.util.deserialiseToken
import chat.rocket.common.model.Token
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.android.AndroidInjection
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

                val isActivityForeground =
                    sharedPreferencesManager.getSharedPreferenceBoolean(
                        KEY_PREFS_ACTIVITY_FOREGROUND
                    )
                if (isActivityForeground) {
                    //inform main activity about saving tokens if it is in foreground
                    //to lead the user to the main activity
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