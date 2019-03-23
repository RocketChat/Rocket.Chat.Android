package chat.rocket.android.analytics

import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.analytics.event.SubscriptionTypeEvent
import chat.rocket.android.server.domain.AnalyticsTrackingInteractor
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import javax.inject.Inject

class AnalyticsManager @Inject constructor(
    private val analyticsTrackingInteractor: AnalyticsTrackingInteractor,
    getCurrentServerInteractor: GetCurrentServerInteractor,
    getAccountsInteractor: GetAccountsInteractor,
    private val analytics: List<Analytics>
) {
    val serverUrl = getCurrentServerInteractor.get()
    val accounts = getAccountsInteractor.get()

    fun logLogin(
        event: AuthenticationEvent,
        loginSucceeded: Boolean
    ) {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logLogin(event, loginSucceeded) }
        }
    }

    fun logSignUp(
        event: AuthenticationEvent,
        signUpSucceeded: Boolean
    ) {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logSignUp(event, signUpSucceeded) }
        }
    }

    fun logScreenView(event: ScreenViewEvent) {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logScreenView(event) }
        }
    }

    fun logMessageSent(event: SubscriptionTypeEvent) {
        if (analyticsTrackingInteractor.get() && serverUrl != null) {
            analytics.forEach { it.logMessageSent(event, serverUrl) }
        }
    }

    fun logMediaUploaded(event: SubscriptionTypeEvent, mimeType: String) {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMediaUploaded(event, mimeType) }
        }
    }

    fun logReaction(event: SubscriptionTypeEvent) {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logReaction(event) }
        }
    }

    fun logServerSwitch() {
        if (analyticsTrackingInteractor.get() && serverUrl != null) {
            analytics.forEach { it.logServerSwitch(serverUrl, accounts.size) }
        }
    }

    fun logOpenAdmin() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logOpenAdmin() }
        }
    }

    fun logResetPassword(resetPasswordSucceeded: Boolean) {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logResetPassword(resetPasswordSucceeded) }
        }
    }

    fun logVideoConference(event: SubscriptionTypeEvent) {
        if (analyticsTrackingInteractor.get() && serverUrl != null) {
            analytics.forEach { it.logVideoConference(event, serverUrl) }
        }
    }

    fun logMessageActionAddReaction() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionAddReaction() }
        }
    }

    fun logMessageActionReply() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionReply() }
        }
    }

    fun logMessageActionQuote() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionQuote() }
        }
    }

    fun logMessageActionPermalink() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionPermalink() }
        }
    }

    fun logMessageActionCopy() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionCopy() }
        }
    }

    fun logMessageActionEdit() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionEdit() }
        }
    }

    fun logMessageActionInfo() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionInfo() }
        }
    }

    fun logMessageActionStar() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionStar() }
        }
    }

    fun logMessageActionPin() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionPin() }
        }
    }

    fun logMessageActionReport() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionReport() }
        }
    }

    fun logMessageActionDelete() {
        if (analyticsTrackingInteractor.get()) {
            analytics.forEach { it.logMessageActionDelete() }
        }
    }
}
