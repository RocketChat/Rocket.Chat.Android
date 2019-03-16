package chat.rocket.android.videoconference.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import chat.rocket.android.videoconference.presenter.JitsiVideoConferenceView
import chat.rocket.android.videoconference.presenter.VideoConferencePresenter
import dagger.android.AndroidInjection
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetView
import org.jitsi.meet.sdk.JitsiMeetViewListener
import timber.log.Timber
import javax.inject.Inject

fun Context.videoConferenceIntent(chatRoomId: String, chatRoomType: String): Intent =
    Intent(this, VideoConferenceActivity::class.java)
        .putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)
        .putExtra(INTENT_CHAT_ROOM_TYPE, chatRoomType)

private const val INTENT_CHAT_ROOM_ID = "chat_room_id"
private const val INTENT_CHAT_ROOM_TYPE = "chat_room_type"

class VideoConferenceActivity : JitsiMeetActivity(), JitsiVideoConferenceView,
    JitsiMeetViewListener {
    @Inject
    lateinit var presenter: VideoConferencePresenter
    private lateinit var chatRoomId: String
    private lateinit var chatRoomType: String
    private var view: JitsiMeetView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        chatRoomId = intent.getStringExtra(INTENT_CHAT_ROOM_ID)
        requireNotNull(chatRoomId) { "no chat_room_id provided in Intent extras" }
        chatRoomType = intent.getStringExtra(INTENT_CHAT_ROOM_TYPE)
        requireNotNull(chatRoomType) { "no chat_room_type provided in Intent extras" }

        view = JitsiMeetView(this)
        view?.listener = this
        setContentView(view)

        presenter.setup(chatRoomId, chatRoomType)
        presenter.initVideoConference()
    }

    override fun onConferenceWillJoin(map: MutableMap<String, Any>?) =
        logJitsiMeetViewState("Joining video conferencing", map)

    override fun onConferenceJoined(map: MutableMap<String, Any>?) =
        logJitsiMeetViewState("Joined video conferencing", map)

    override fun onConferenceWillLeave(map: MutableMap<String, Any>?) =
        logJitsiMeetViewState("Leaving video conferencing", map)

    override fun onConferenceLeft(map: MutableMap<String, Any>?) {
        logJitsiMeetViewState("Left video conferencing", map)
        finishJitsiVideoConference()
    }

    override fun onLoadConfigError(map: MutableMap<String, Any>?) =
        logJitsiMeetViewState("Error loading video conference config", map)

    override fun onConferenceFailed(map: MutableMap<String, Any>?) =
        logJitsiMeetViewState("Video conference failed", map)

    override fun startJitsiVideoConference(url: String, name: String?) {
        view?.loadURLObject(
            bundleOf(
                "config" to bundleOf(
                    "startWithAudioMuted" to true,
                    "startWithVideoMuted" to true
                ),
                "context" to bundleOf(
                    "user" to bundleOf("name" to name),
                    "iss" to "rocketchat-android"
                ),
                "url" to url
            )
        )
    }

    override fun finishJitsiVideoConference() {
        presenter.invalidateTimer()
        view?.dispose()
        view = null
        finish()
    }

    override fun logJitsiMeetViewState(message: String, map: MutableMap<String, Any>?) =
        Timber.i("$message:  $map")
}
