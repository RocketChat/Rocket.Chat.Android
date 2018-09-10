package chat.rocket.android.util

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging

fun invalidateFirebaseToken(token: String) {
    FirebaseInstanceId.getInstance().deleteToken(token, FirebaseMessaging.INSTANCE_ID_SCOPE)
}
