package chat.rocket.android.server.infraestructure

import android.app.job.JobInfo
import android.app.job.JobScheduler
import chat.rocket.android.server.domain.JobSchedulerInteractor
import timber.log.Timber
import javax.inject.Inject

/**
 * Uses the Android framework [JobScheduler].
 */
class JobSchedulerInteractorImpl @Inject constructor(
    private val jobScheduler: JobScheduler,
    private val jobInfo: JobInfo
) : JobSchedulerInteractor {

    override fun scheduleSendingMessages() {
        Timber.d("Scheduling unsent messages to send...")
        jobScheduler.schedule(jobInfo)
    }
}