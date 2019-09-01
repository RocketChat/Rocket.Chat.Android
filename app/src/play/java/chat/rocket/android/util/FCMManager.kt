package chat.rocket.android.util

import com.google.firebase.iid.FirebaseInstanceId
import timber.log.Timber

fun invalidateFirebaseToken() {
    Timber.d("Invalidating push notification token")
    FirebaseInstanceId.getInstance().deleteInstanceId()
}