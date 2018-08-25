package chat.rocket.android.server.domain

import javax.inject.Inject

class AnalyticsTrackingInteractor @Inject constructor(val repository: AnalyticsTrackingRepository) {
    fun save(isAnalyticsTrackingEnable: Boolean) = repository.save(isAnalyticsTrackingEnable)
    fun get(): Boolean = repository.get()
}