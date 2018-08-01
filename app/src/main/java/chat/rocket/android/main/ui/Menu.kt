package chat.rocket.android.main.ui

import android.view.Menu
import android.view.MenuItem
import chat.rocket.android.R

private const val MENU_SECTION_ONE = 1
private const val MENU_SECTION_TWO = 2
private const val MENU_SECTION_THREE = 3

const val MENU_ACTION_CHATS = 1
private const val MENU_ACTION_CREATE_CHANNEL = 2
const val MENU_ACTION_PROFILE = 3
private const val MENU_ACTION_SETTINGS = 4
private const val MENU_ACTION_ADMIN_PANEL = 5
private const val MENU_ACTION_LOGOUT = 6

internal fun MainActivity.setupMenu(menu: Menu) {
    menu.add(
        MENU_SECTION_ONE,
        MENU_ACTION_CHATS,
        Menu.NONE,
        R.string.title_chats
    ).setIcon(R.drawable.ic_chat_bubble_black_24dp)
        .isChecked = true

    menu.add(
        MENU_SECTION_ONE,
        MENU_ACTION_CREATE_CHANNEL,
        Menu.NONE,
        R.string.action_create_channel
    ).setIcon(R.drawable.ic_create_black_24dp)

    menu.add(
        MENU_SECTION_TWO,
        MENU_ACTION_PROFILE,
        Menu.NONE,
        R.string.title_profile
    ).setIcon(R.drawable.ic_person_black_24dp)

    menu.add(
        MENU_SECTION_TWO,
        MENU_ACTION_SETTINGS,
        Menu.NONE,
        R.string.title_settings
    ).setIcon(R.drawable.ic_settings_black_24dp)

    if (userHelper.isAdmin()) {
        menu.add(
            MENU_SECTION_TWO,
            MENU_ACTION_ADMIN_PANEL,
            Menu.NONE,
            R.string.title_admin_panel
        ).setIcon(R.drawable.ic_settings_black_24dp)
    }

    menu.add(
        MENU_SECTION_THREE,
        MENU_ACTION_LOGOUT,
        Menu.NONE,
        R.string.action_logout
    ).setIcon(R.drawable.ic_logout_black_24dp)

    menu.setGroupCheckable(MENU_SECTION_ONE, true, true)
    menu.setGroupCheckable(MENU_SECTION_TWO, true, true)
    menu.setGroupCheckable(MENU_SECTION_THREE, true, true)
}

internal fun MainActivity.onNavDrawerItemSelected(menuItem: MenuItem) {
    when (menuItem.itemId) {
        MENU_ACTION_CHATS -> presenter.toChatList()
        MENU_ACTION_CREATE_CHANNEL -> presenter.toCreateChannel()
        MENU_ACTION_PROFILE -> presenter.toUserProfile()
        MENU_ACTION_SETTINGS -> presenter.toSettings()
        MENU_ACTION_ADMIN_PANEL -> presenter.toAdminPanel()
        MENU_ACTION_LOGOUT -> presenter.logout()
    }
}
