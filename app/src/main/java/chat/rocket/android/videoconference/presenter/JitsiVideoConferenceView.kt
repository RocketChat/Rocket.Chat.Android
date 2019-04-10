package chat.rocket.android.videoconference.presenter

interface JitsiVideoConferenceView {

    /**
     * Starts the Jitsi video conference.
     *
     * @param url The video conference URL to be loaded.
     * @param name The user name to be show on the video conference.
     */
    fun startJitsiVideoConference(url: String, name: String?)

    /**
     * Finishes the Jitsi video conference.
     */
    fun finishJitsiVideoConference()

    /**
     * Logs the state of the Jitsi Meet conference displayed in a JitsiMeetView.
     *
     * @param message The message to log.
     * @param map the map information by Jitsi
     */
    fun logJitsiMeetViewState(message: String, map: MutableMap<String, Any>?)
}