package chat.rocket.android.push

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import timber.log.Timber

fun retrieveCurrentPushToken(): String? {
    var pushNotificationToken: String? = null
    FirebaseInstanceId.getInstance().instanceId
        .addOnCompleteListener(OnCompleteListener { task ->

            if (!task.isSuccessful) {
                Timber.w("Unable to retrieve current push notification token")
                return@OnCompleteListener
            }

            // Get new Instance ID token
            task.result?.token?.let {
                Timber.d("Retrieve current push notification token: $it")
                pushNotificationToken = it
            }
        })
    return pushNotificationToken
}