package chat.rocket.android.videoconference.presenter

import java.net.URL

interface JitsiVideoConferenceView {

    /**
     * Setups the video conference.
     *
     * @param serverURL The server URL of the video conference.
     */
    fun setupVideoConference(serverURL: URL)

    /**
     * Starts the video conference.
     *
     * @param room The room of the video conference.
     */
    fun startVideoConference(room: String)

    /**
     * Finishes the video conference.
     */
    fun finishVideoConference()

    /**
     * Logs the state of the Jitsi Meet conference displayed in a JitsiMeetView.
     *
     * @param message The message to log.
     * @param map the map information by Jitsi
     */
    fun logJitsiMeetViewState(message: String, map: MutableMap<String, Any>?)
}