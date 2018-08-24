package chat.rocket.android.push

import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.core.internal.rest.registerPushToken
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import dagger.android.AndroidInjection
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class FirebaseTokenService : FirebaseInstanceIdService() {

    @Inject
    lateinit var factory: RocketChatClientFactory
    @Inject
    lateinit var getCurrentServerInteractor: GetCurrentServerInteractor

    @Inject
    lateinit var localRepository: LocalRepository

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onTokenRefresh() {
        try {
            val fcmToken = FirebaseInstanceId.getInstance().token
            val currentServer = getCurrentServerInteractor.get()
            val client = currentServer?.let { factory.create(currentServer) }

            fcmToken?.let {
                localRepository.save(LocalRepository.KEY_PUSH_TOKEN, fcmToken)
                client?.let {
                    launch {
                        try {
                            Timber.d("Registering push token: $fcmToken for ${client.url}")
                            retryIO("register push token") { client.registerPushToken(fcmToken) }
                        } catch (ex: RocketChatException) {
                            Timber.e(ex, "Error registering push token")
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex, "Error refreshing Firebase TOKEN")
        }
    }
}