package chat.rocket.android.preferences.presentation

interface PreferencesView {

    /**
     * Setups the analytics tracking view.
     *
     * @param isAnalyticsTrackingEnabled Whether the analytics tracking is enabled
     */
    fun setupAnalyticsTrackingView(isAnalyticsTrackingEnabled: Boolean)
}