package chat.rocket.android

import chat.rocket.android.service.KeepAliveJob
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

class RocketChatJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        when (tag) {
            KeepAliveJob.TAG -> return KeepAliveJob()
            else -> return null
        }
    }
}