package chat.rocket.android.helper

object Constants {
    const val CHATROOM_SORT_TYPE_KEY: String = "chatroom_sort_type"
    const val CHATROOM_GROUP_BY_TYPE_KEY: String = "chatroom_group_by_type"
    const val CHATROOM_GROUP_FAVOURITES_KEY: String = "chatroom_group_favourites"

    //Used to sort chat rooms
    const val CHATROOM_CHANNEL = 0
    const val CHATROOM_PRIVATE_GROUP = 1
    const val CHATROOM_DM = 2
    const val CHATROOM_LIVE_CHAT = 3
}

object DataMeasure {
    const val BYTE = 1
    const val KILOBYTE = 1024
    const val MEGABYTE = KILOBYTE * 1024
    const val GIGABYTE = MEGABYTE * 1024
}

object ChatRoomsSortOrder {
    const val ALPHABETICAL: Int = 0
    const val ACTIVITY: Int = 1
}