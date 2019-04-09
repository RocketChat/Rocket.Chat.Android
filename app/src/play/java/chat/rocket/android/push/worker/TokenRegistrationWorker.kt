package chat.rocket.android.push.worker

import androidx.work.Worker
import chat.rocket.android.dagger.injector.AndroidWorkerInjection
import chat.rocket.android.extensions.await
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.registerPushToken
import chat.rocket.common.util.ifNull
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

class TokenRegistrationWorker : Worker() {

    @Inject
    lateinit var factory: RocketChatClientFactory
    @Inject
    lateinit var getAccountsInteractor: GetAccountsInteractor
    @Inject
    lateinit var localRepository: LocalRepository

    override fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        runBlocking {
            val token = inputData.getString("token") ?: refreshToken()

            token?.let { fcmToken ->
                localRepository.save(LocalRepository.KEY_PUSH_TOKEN, fcmToken)
                factory.registerPushToken(fcmToken, getAccountsInteractor.get())
            }.ifNull {
                Timber.d("Unavailable FCM Token...")
            }
        }

        return Result.SUCCESS
    }

    private fun refreshToken(): String? {
        return runBlocking {
            try {
                FirebaseInstanceId.getInstance().instanceId.await().token
            } catch (ex: Exception) {
                Timber.e(ex, "Error refreshing Firebase TOKEN")
                null
            }
        }
    }
}