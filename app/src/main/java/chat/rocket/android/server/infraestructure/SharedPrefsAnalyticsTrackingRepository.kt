package chat.rocket.android.server.infraestructure

import android.content.SharedPreferences
import chat.rocket.android.server.domain.AnalyticsTrackingRepository

private const val ANALYTICS_TRACKING_KEY = "ANALYTICS_TRACKING_KEY"

class SharedPrefsAnalyticsTrackingRepository(private val preferences: SharedPreferences) :
    AnalyticsTrackingRepository {

    override fun save(isAnalyticsTrackingEnable: Boolean) =
        preferences.edit().putBoolean(ANALYTICS_TRACKING_KEY, isAnalyticsTrackingEnable).apply()

    override fun get() = preferences.getBoolean(ANALYTICS_TRACKING_KEY, true)
}