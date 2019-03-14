package chat.rocket.android.videoconferencing.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import chat.rocket.android.videoconferencing.presenter.VideoConferencingPresenter
import chat.rocket.android.videoconferencing.presenter.VideoConferencingView
import dagger.android.AndroidInjection
import org.jitsi.meet.sdk.JitsiMeetActivity
import java.net.URL
import javax.inject.Inject


fun Context.videoConferencingIntent(chatRoomId: String): Intent =
    Intent(this, VideoConferencingActivity::class.java).putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)

private const val INTENT_CHAT_ROOM_ID = "chat_room_id"

class VideoConferencingActivity : JitsiMeetActivity(), VideoConferencingView {
    @Inject
    lateinit var presenter: VideoConferencingPresenter
    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)
        requireNotNull(chatRoomId) { "no chat_room_id provided in Intent extras" }

        presenter.setup(chatRoomId)
        presenter.setupVideoConferencing()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.invalidateTimer()
    }

    override fun startVideoConferencing(url: URL) = loadURL(url)
}
