package chat.rocket.android.videoconference.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import chat.rocket.android.videoconference.presenter.JitsiVideoConferenceView
import chat.rocket.android.videoconference.presenter.VideoConferencePresenter
import dagger.android.AndroidInjection
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetViewListener
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

fun Context.videoConferenceIntent(chatRoomId: String, chatRoomType: String): Intent =
    Intent(this, VideoConferenceActivity::class.java)
        .putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)
        .putExtra(INTENT_CHAT_ROOM_TYPE, chatRoomType)

private const val INTENT_CHAT_ROOM_ID = "chat_room_id"
private const val INTENT_CHAT_ROOM_TYPE = "chat_room_type"

class VideoConferenceActivity : AppCompatActivity(), JitsiVideoConferenceView,
    JitsiMeetViewListener {
    @Inject lateinit var presenter: VideoConferencePresenter
    private lateinit var chatRoomId: String
    private lateinit var chatRoomType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)
        chatRoomType = intent.getStringExtra(INTENT_CHAT_ROOM_TYPE)

        with(presenter) {
            setup(chatRoomId, chatRoomType)
            initVideoConference()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishVideoConference()
    }

    override fun onConferenceWillJoin(map: MutableMap<String, Any>?) =
        logJitsiMeetViewState("Joining video conferencing", map)

    override fun onConferenceJoined(map: MutableMap<String, Any>?) =
        logJitsiMeetViewState("Joined video conferencing", map)

    override fun onConferenceTerminated(map: MutableMap<String, Any>?) {
        map?.let {
            if (it.containsKey("error")) {
                logJitsiMeetViewState("Terminated video conferencing with error", map)
            } else {
                logJitsiMeetViewState("Terminated video conferencing", map)
            }
        }
        finishVideoConference()
    }

    override fun setupVideoConference(serverURL: URL) =
        JitsiMeet.setDefaultConferenceOptions(
            JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setWelcomePageEnabled(false)
                .build()
        )

    override fun startVideoConference(room: String) =
        JitsiMeetActivity.launch(
            this, JitsiMeetConferenceOptions.Builder()
                .setRoom(room)
                .build()
        )

    override fun finishVideoConference() {
        presenter.invalidateTimer()
        finish()
    }

    override fun logJitsiMeetViewState(message: String, map: MutableMap<String, Any>?) =
        Timber.i("$message:  $map")
}
