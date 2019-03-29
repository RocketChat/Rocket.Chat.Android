package chat.rocket.android.chatdetails.ui

import android.view.Menu
import android.view.MenuItem
import chat.rocket.android.R
import chat.rocket.android.server.domain.isJitsiEnabled
import chat.rocket.android.server.domain.isJitsiEnabledForChannels
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf

internal fun ChatDetailsFragment.setupMenu(menu: Menu) {
    serverUrl.get()?.let {
        with(settings.get(it)) {
            if (isJitsiEnabled()) {
                if (roomTypeOf(chatRoomType) !is RoomType.DirectMessage && !isJitsiEnabledForChannels()) {
                    return
                }
                menu.add(
                    Menu.NONE,
                    MENU_ACTION_VIDEO_CALL,
                    Menu.NONE,
                    R.string.msg_video_call
                ).setIcon(R.drawable.ic_video_white_24dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
        }
    }

    if (isFavorite) {
        menu.add(
            Menu.NONE,
            MENU_ACTION_FAVORITE_REMOVE_FAVORITE,
            Menu.NONE,
            R.string.action_remove_favorite
        ).setIcon(R.drawable.ic_star_yellow_24dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    } else {
        menu.add(
            Menu.NONE,
            MENU_ACTION_FAVORITE_REMOVE_FAVORITE,
            Menu.NONE,
            R.string.action_favorite
        ).setIcon(R.drawable.ic_star_border_white_24dp)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }
}

internal fun ChatDetailsFragment.setOnMenuItemClickListener(item: MenuItem) {
    if (item.itemId == MENU_ACTION_FAVORITE_REMOVE_FAVORITE) {
        presenter.toggleFavoriteChatRoom(chatRoomId, isFavorite)
    } else if (item.itemId == MENU_ACTION_VIDEO_CALL) {
        presenter.toVideoConference(chatRoomId, chatRoomType)
    }
}
