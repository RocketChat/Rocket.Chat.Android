package chat.rocket.android.services

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.SaveCurrentServerInteractor
import chat.rocket.android.server.TokenRepository
import chat.rocket.android.util.*
import chat.rocket.common.model.Token
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import dagger.android.AndroidInjection
import javax.inject.Inject

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

    override fun onDataChanged(dataEventBuffer: DataEventBuffer?) {
        if (dataEventBuffer != null) {
            for (event in dataEventBuffer) {
                val path: String = event.dataItem.uri.path
                if (SERVER_PATH == path) {
                    //save current server
                    val currentServerUrl =
                        DataMapItem.fromDataItem(event.dataItem).dataMap.getString(SERVER_URL_KEY)
                    serverInteractor.save(currentServerUrl)
                } else if (TOKEN_PATH == path) {
                    //save token
                    val currentServer = getCurrentServerInteractor.get()
                    if (currentServer != null) {
                        val tokenUserId =
                            DataMapItem.fromDataItem(event.dataItem).dataMap.getString(
                                TOKEN_USER_ID_IDENTIFIER
                            )
                        val tokenAuth =
                            DataMapItem.fromDataItem(event.dataItem).dataMap.getString(
                                TOKEN_AUTH_IDENTIFIER
                            )

                        val loginToken = Token(tokenUserId, tokenAuth)
                        tokenRepository.save(currentServer, loginToken)

                        val isActivityForeground =
                            sharedPreferencesManager.getSharedPreferenceBoolean(
                                KEY_PREFS_ACTIVITY_FOREGROUND
                            )
                        if (isActivityForeground){
                            //inform main activity about saving tokens if it is in foreground
                            //to lead the user to the main activity
                        }
                    }
                }
            }
        }

    }
}