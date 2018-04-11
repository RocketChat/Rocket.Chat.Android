package chat.rocket.android.chatroom.service

import android.app.job.JobParameters
import android.app.job.JobService
import chat.rocket.android.server.domain.CurrentServerRepository
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class MessageService : JobService() {
    @Inject
    lateinit var factory: ConnectionManagerFactory
    @Inject
    lateinit var currentServerRepository: CurrentServerRepository

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        launch(CommonPool) {
            val currentServer = currentServerRepository.get()
            if (currentServer != null) {
                val connectionManager = factory.create(currentServer)
            }
            jobFinished(params, false)
        }
        return true
    }
}