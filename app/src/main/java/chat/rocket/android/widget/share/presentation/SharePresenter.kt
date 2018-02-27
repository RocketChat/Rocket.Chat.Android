package chat.rocket.android.widget.share.presentation

import android.net.Uri
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import javax.inject.Inject

class SharePresenter @Inject constructor(private val view: ShareView,
                                         serverInteractor: GetCurrentServerInteractor,
                                         factory: RocketChatClientFactory) {
    private val client = factory.create(serverInteractor.get()!!)

    /**
     * Show the room where to share text to.
     *
     * @param roomId The id of the room
     */
    fun openRoomForSharingText(roomId: String) {
        val content = ""
        view.shareToRoom(roomId, content)
    }

    /**
     * Show the room where to share images to.
     *
     * @param roomId The id of the room
     */
    fun openRoomForSharingImages(roomId: String) {
        val uri = Uri.parse("")
        val name = ""
        view.shareToRoom(roomId, uri, name)
    }

    /**
     * Get all completion possibilities back to the view.
     *
     * @param The prefix to use for searching.
     */
    fun getAvailableRooms(prefix: String? = null) {
        val rooms = listOf("GENERAL")
        if (prefix == null || prefix.isEmpty()) {
            // show all rooms
        } else {
            // show rooms with prefix
        }
        view.showRoomsForSharing(rooms)
    }
}