package chat.rocket.android.analytics

import android.content.Context
import android.os.Bundle
import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.analytics.event.SubscriptionTypeEvent
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class GoogleAnalyticsForFirebase @Inject constructor(val context: Context) :
    Analytics {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logLogin(event: AuthenticationEvent, loginSucceeded: Boolean) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle(2).apply {
            putString(FirebaseAnalytics.Param.METHOD, event.methodName)
            putLong(FirebaseAnalytics.Param.SUCCESS, if (loginSucceeded) 1 else 0)
        })
    }

    override fun logSignUp(event: AuthenticationEvent, signUpSucceeded: Boolean) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, Bundle(2).apply {
            putString(FirebaseAnalytics.Param.METHOD, event.methodName)
            putLong(FirebaseAnalytics.Param.SUCCESS, if (signUpSucceeded) 1 else 0)
        })
    }

    override fun logScreenView(event: ScreenViewEvent) {
        firebaseAnalytics.logEvent("screen_view", Bundle(1).apply {
            putString("screen", event.screenName)
        })
    }

    override fun logMessageSent(event: SubscriptionTypeEvent, serverUrl: String) {
        firebaseAnalytics.logEvent("message_sent", Bundle(2).apply {
            putString("subscription_type", event.subscriptionTypeName)
            putString("server", serverUrl)
        })
    }

    override fun logMediaUploaded(event: SubscriptionTypeEvent, mimeType: String) {
        firebaseAnalytics.logEvent("media_upload", Bundle(2).apply {
            putString("subscription_type", event.subscriptionTypeName)
            putString("media_type", mimeType)
        })
    }

    override fun logReaction(event: SubscriptionTypeEvent) {
        firebaseAnalytics.logEvent("reaction", Bundle(2).apply {
            putString("subscription_type", event.subscriptionTypeName)
        })
    }

    override fun logServerSwitch(serverUrl: String, serverCount: Int) {
        firebaseAnalytics.logEvent("server_switch", Bundle(2).apply {
            putString("server_url", serverUrl)
            putInt("server_count", serverCount)
        })
    }

    override fun logOpenAdmin() = firebaseAnalytics.logEvent("open_admin", null)

    override fun logResetPassword(resetPasswordSucceeded: Boolean) =
        firebaseAnalytics.logEvent("reset_password", Bundle(2).apply {
            putBoolean("resetPasswordSucceeded", resetPasswordSucceeded)
        })

    override fun logVideoConference(event: SubscriptionTypeEvent, serverUrl: String) {
        firebaseAnalytics.logEvent("video_conference", Bundle(2).apply {
            putString("subscription_type", event.subscriptionTypeName)
            putString("server", serverUrl)
        })
    }

    override fun logMessageActionAddReaction() = firebaseAnalytics.logEvent("message_action_add_reaction", null)

    override fun logMessageActionReply() = firebaseAnalytics.logEvent("message_action_reply", null)

    override fun logMessageActionQuote() = firebaseAnalytics.logEvent("message_action_quote", null)

    override fun logMessageActionPermalink() = firebaseAnalytics.logEvent("message_action_permalink", null)

    override fun logMessageActionCopy() = firebaseAnalytics.logEvent("message_action_copy", null)

    override fun logMessageActionEdit() = firebaseAnalytics.logEvent("message_action_edit", null)

    override fun logMessageActionInfo() = firebaseAnalytics.logEvent("message_action_info", null)

    override fun logMessageActionStar() = firebaseAnalytics.logEvent("message_action_star", null)

    override fun logMessageActionPin() = firebaseAnalytics.logEvent("message_action_pin", null)

    override fun logMessageActionReport() = firebaseAnalytics.logEvent("message_action_report", null)

    override fun logMessageActionDelete() = firebaseAnalytics.logEvent("message_action_delete", null)
}
