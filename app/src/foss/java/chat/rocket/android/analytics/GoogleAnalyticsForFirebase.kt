package chat.rocket.android.analytics

import android.content.Context
import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.analytics.event.SubscriptionTypeEvent
import javax.inject.Inject

class GoogleAnalyticsForFirebase @Inject constructor(val  context: Context) :
    Analytics {

    override fun logLogin(event: AuthenticationEvent, loginSucceeded: Boolean) {
        // Do absolutely nothing
    }

    override fun logSignUp(event: AuthenticationEvent, signUpSucceeded: Boolean) {
        // Do absolutely nothing
    }

    override fun logScreenView(event: ScreenViewEvent) {
        // Do absolutely nothing
    }

    override fun logMessageSent(event: SubscriptionTypeEvent, serverUrl: String) {
        // Do absolutely nothing
    }

    override fun logMediaUploaded(event: SubscriptionTypeEvent, mimeType: String) {
        // Do absolutely nothing
    }

    override fun logReaction(event: SubscriptionTypeEvent) {
        // Do absolutely nothing
    }

    override fun logServerSwitch(serverUrl: String, serverCount: Int) {
        // Do absolutely nothing
    }
}
