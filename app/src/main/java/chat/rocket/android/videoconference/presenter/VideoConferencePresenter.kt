package chat.rocket.android.videoconference.presenter

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.JitsiHelper
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.updateJitsiTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timer

class VideoConferencePresenter @Inject constructor(
    private val view: JitsiVideoConferenceView,
    private val strategy: CancelStrategy,
    private val currentServerRepository: CurrentServerRepository,
    private val connectionManagerFactory: ConnectionManagerFactory,
    private val settings: GetSettingsInteractor,
    private val userHelp: UserHelper
) {
    private lateinit var client: RocketChatClient
    private lateinit var publicSettings: PublicSettings
    private lateinit var chatRoomId: String
    private lateinit var timer: Timer

    fun setup(chatRoomId: String) {
        currentServerRepository.get()?.let {
            client = connectionManagerFactory.create(it).client
            publicSettings = settings.get(it)
        }
        this.chatRoomId = chatRoomId
    }

    fun initVideoConference() {
        launchUI(strategy) {
            try {
                with(publicSettings) {
                    view.startJitsiVideoConference(
                        JitsiHelper.getJitsiUrl(
                            isJitsiSSL(),
                            jitsiDomain(),
                            jitsiPrefix(),
                            uniqueIdentifier(),
                            chatRoomId
                        ),
                        userHelp.user()?.username
                    )

                    updateJitsiTimeout()
                }
            } catch (ex: Exception) {
                Timber.e(ex)
                view.finishJitsiVideoConference()
            }
        }
    }

    fun invalidateTimer() = timer.cancel()

    // Jitsi update call needs to be called every 10 seconds to make sure call is not ended and is available to web users.
    private fun updateJitsiTimeout() {
        timer = timer(daemon = false, initialDelay = 0L, period = 10000) {
            GlobalScope.launch(Dispatchers.IO + strategy.jobs) {
                client.updateJitsiTimeout(chatRoomId)
            }
        }
    }
}

