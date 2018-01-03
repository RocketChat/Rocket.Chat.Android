package chat.rocket.android.push

import chat.rocket.android.R
import chat.rocket.android.dagger.module.AppModule
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.push.di.DaggerPushComponent
import chat.rocket.android.push.di.PushModule
import chat.rocket.common.RocketChatException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.registerPushToken
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import com.google.firebase.iid.FirebaseInstanceIdService
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class PushTokenService : FirebaseInstanceIdService() {

    @Inject
    lateinit var client: RocketChatClient

    @Inject
    lateinit var localRepository: LocalRepository

    override fun onCreate() {
        super.onCreate()
        DaggerPushComponent.builder()
                .appModule(AppModule())
                .pushModule(PushModule(this))
                .build()
                .inject(this)
    }


    override fun onTokenRefresh() {
        val gcmToken = InstanceID.getInstance(this)
                .getToken(getString(R.string.gcm_defaultSenderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE), null)

        gcmToken?.let {
            localRepository.save(LocalRepository.KEY_PUSH_TOKEN, gcmToken)
            launch {
                try {
                    client.registerPushToken(gcmToken)
                } catch (ex: RocketChatException) {
                    Timber.e(ex)
                }
            }
        }
    }
}