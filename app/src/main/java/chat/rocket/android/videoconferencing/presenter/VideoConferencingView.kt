package chat.rocket.android.videoconferencing.presenter

import java.net.URL

interface VideoConferencingView {

    /**
     * Starts the video conferencing.
     *
     * @param url The video conferencing URL to be loaded.
     */
    fun startVideoConferencing(url: URL)
}