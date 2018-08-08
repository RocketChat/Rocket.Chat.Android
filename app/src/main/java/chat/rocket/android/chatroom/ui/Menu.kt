package chat.rocket.android.chatroom.ui

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import chat.rocket.android.R
import chat.rocket.android.util.extension.onQueryTextListener
import chat.rocket.common.model.RoomType

internal fun ChatRoomFragment.setupMenu(menu: Menu) {
    setupSearchMessageMenuItem(menu, requireContext())
    setupFavoriteMenuItem(menu)

    menu.add(
        Menu.NONE,
        MENU_ACTION_PINNED_MESSAGES,
        Menu.NONE,
        R.string.title_pinned_messages
    )

    menu.add(
        Menu.NONE,
        MENU_ACTION_FAVORITE_MESSAGES,
        Menu.NONE,
        R.string.title_favorite_messages
    )

    if (chatRoomType != RoomType.DIRECT_MESSAGE && !disableMenu) {
        menu.add(
            Menu.NONE,
            MENU_ACTION_MEMBER,
            Menu.NONE,
            R.string.title_members_list
        )

        menu.add(
            Menu.NONE,
            MENU_ACTION_MENTIONS,
            Menu.NONE,
            R.string.msg_mentions
        )
    }

    if (!disableMenu) {
        menu.add(
            Menu.NONE,
            MENU_ACTION_FILES,
            Menu.NONE,
            R.string.title_files
        )
    }
}

internal fun ChatRoomFragment.setOnMenuItemClickListener(item: MenuItem) {
    when (item.itemId) {
        MENU_ACTION_FAVORITE_UNFAVORITE_CHAT -> presenter.toggleFavoriteChatRoom(
            chatRoomId,
            isFavorite
        )
        MENU_ACTION_MEMBER -> presenter.toMembersList(chatRoomId)
        MENU_ACTION_MENTIONS -> presenter.toMentions(chatRoomId)
        MENU_ACTION_PINNED_MESSAGES -> presenter.toPinnedMessageList(chatRoomId)
        MENU_ACTION_FAVORITE_MESSAGES -> presenter.toFavoriteMessageList(chatRoomId)
        MENU_ACTION_FILES -> presenter.toFileList(chatRoomId)
    }
}

private fun ChatRoomFragment.setupSearchMessageMenuItem(menu: Menu, context: Context) {
    val searchItem = menu.add(
        Menu.NONE,
        Menu.NONE,
        Menu.NONE,
        R.string.title_search_message
    ).setActionView(SearchView(context))
        .setIcon(R.drawable.ic_search_white_24dp)
        .setShowAsActionFlags(
            MenuItem.SHOW_AS_ACTION_IF_ROOM or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
        )

    (searchItem?.actionView as? SearchView)?.let {
        // TODO: Check why we need to stylize the search text programmatically instead of by defining it in the styles.xml (ChatRoom.SearchView)
        stylizeSearchView(it, context)
        setupSearchViewTextListener(it)
        if (it.isIconified) {
            isSearchTermQueried = false
        }
    }
}

private fun stylizeSearchView(searchView: SearchView, context: Context) {
    val searchText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
    searchText.setTextColor(ResourcesCompat.getColor(context.resources, R.color.color_white, null))
    searchText.setHintTextColor(
        ResourcesCompat.getColor(context.resources, R.color.color_white, null)
    )
}

private fun ChatRoomFragment.setupSearchViewTextListener(searchView: SearchView) {
    searchView.onQueryTextListener {
        // TODO: We use isSearchTermQueried to avoid querying when the search view is expanded but the user doesn't start typing. Check for a native solution.
        if (it.isEmpty() && isSearchTermQueried) {
            presenter.loadMessages(chatRoomId, chatRoomType, clearDataSet = true)
        } else if (it.isNotEmpty()) {
            presenter.searchMessages(chatRoomId, it)
            isSearchTermQueried = true
        }
    }
}

private fun ChatRoomFragment.setupFavoriteMenuItem(menu: Menu) {
    if (isFavorite) {
        menu.add(
            Menu.NONE,
            MENU_ACTION_FAVORITE_UNFAVORITE_CHAT,
            Menu.NONE,
            R.string.title_unfavorite_chat
        ).setIcon(R.drawable.ic_star_yellow_24dp)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    } else {
        menu.add(
            Menu.NONE,
            MENU_ACTION_FAVORITE_UNFAVORITE_CHAT,
            Menu.NONE,
            R.string.title_favorite_chat
        ).setIcon(R.drawable.ic_star_border_white_24dp)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }
}