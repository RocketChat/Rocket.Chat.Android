package chat.rocket.android.widget.roomupdate

import chat.rocket.common.model.RoomType

interface UpdateObserver {
    fun provideRoomId(): String
    fun lastUpdated(): Long
    fun onRoomChanged(name: String, type: RoomType, readOnly: Boolean? = false, updatedAt: Long?)
}

