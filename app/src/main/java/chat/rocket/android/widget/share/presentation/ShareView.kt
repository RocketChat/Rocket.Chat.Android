package chat.rocket.android.widget.share.presentation

import android.net.Uri

interface ShareView {
    /**
     * Share image to room.
     *
     */
    fun shareToRoom(roomId: String, uri: Uri, name: String)

    /**
     * Share text to room.
     */
    fun shareToRoom(roomId: String, content: String)

    /**
     * Show list of rooms available for sharing content.
     */
    fun showRoomsForSharing(rooms: List<String>)
}