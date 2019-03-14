package chat.rocket.android.videoconferencing.presenter

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.JitsiHelper
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.ConnectionManager
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.realtime.updateJitsiTimeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class VideoConferencingPresenter @Inject constructor(
    private val view: VideoConferencingView,
    private val strategy: CancelStrategy,
    private val currentServerRepository: CurrentServerRepository,
    private val connectionManagerFactory: ConnectionManagerFactory,
    private val settings: GetSettingsInteractor
) {
    private lateinit var currentServerUrl: String
    private lateinit var connectionManager: ConnectionManager
    private lateinit var client: RocketChatClient
    private lateinit var publicSettings: PublicSettings
    private lateinit var chatRoomId: String

    fun setup(chatRoomId: String) {
        currentServerRepository.get()?.let {
            currentServerUrl = it
            connectionManager = connectionManagerFactory.create(it)
            client = connectionManager.client
            publicSettings = settings.get(it)
        }
        this.chatRoomId = chatRoomId
    }

    fun setupVideoConferencing() {
        launchUI(strategy) {
            with(publicSettings) {
                view.startVideoConferencing(
                    JitsiHelper.getJitsiUrl(
                        isJitsiSSL(),
                        jitsiDomain(),
                        jitsiPrefix(),
                        uniqueIdentifier(),
                        chatRoomId
                    )
                )
            }
            client.updateJitsiTimeout(chatRoomId)
        }
    }

    private fun updateJitsiTimeout() {
        GlobalScope.launch(Dispatchers.IO + strategy.jobs) {
            client.updateJitsiTimeout(chatRoomId)
        }
    }
}
