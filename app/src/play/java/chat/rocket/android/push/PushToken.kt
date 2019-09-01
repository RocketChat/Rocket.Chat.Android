package chat.rocket.android.push

import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.registerPushToken
import chat.rocket.core.internal.rest.unregisterPushToken
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.runBlocking
import timber.log.Timber

fun retrieveCurrentPushNotificationToken(
    rocketChatClient: RocketChatClient,
    shouldUnregister: Boolean = false
) =
    FirebaseInstanceId.getInstance().instanceId
        .addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.w("Unable to retrieve the current push notification token")
                return@OnCompleteListener
            }

            task.result?.token?.let {
                Timber.d("Retrieve the current push notification token: $it")

                if (shouldUnregister) {
                    unregisterPushNotificationToken(rocketChatClient, it)
                } else {
                    registerPushNotificationToken(rocketChatClient, it)
                }

            }
        })

fun registerPushNotificationToken(rocketChatClient: RocketChatClient, token: String) {
    runBlocking { rocketChatClient.registerPushToken(token) }
}

private fun unregisterPushNotificationToken(rocketChatClient: RocketChatClient, token: String) {
    runBlocking { rocketChatClient.unregisterPushToken(token) }
}
