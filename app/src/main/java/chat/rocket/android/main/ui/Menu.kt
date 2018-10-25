package chat.rocket.android.main.ui

import android.view.Menu
import android.view.MenuItem
import chat.rocket.android.R

internal fun MainActivity.setupMenu(menu: Menu) {
    with(menu) {
        add(
            R.id.menu_section_one,
            R.id.menu_action_chats,
            Menu.NONE,
            R.string.title_chats
        ).setIcon(R.drawable.ic_chat_bubble_black_24dp)
            .isChecked = true

        add(
            R.id.menu_section_one,
            R.id.menu_action_create_channel,
            Menu.NONE,
            R.string.action_create_channel
        ).setIcon(R.drawable.ic_create_black_24dp)

        add(
            R.id.menu_section_two,
            R.id.menu_action_profile,
            Menu.NONE,
            R.string.title_profile
        ).setIcon(R.drawable.ic_person_black_20dp)

        add(
            R.id.menu_section_two,
            R.id.menu_action_settings,
            Menu.NONE,
            R.string.title_settings
        ).setIcon(R.drawable.ic_settings_black_24dp)

        if (permissions.canSeeTheAdminPanel()) {
            add(
                R.id.menu_section_two,
                R.id.menu_action_admin_panel,
                Menu.NONE,
                R.string.title_admin_panel
            ).setIcon(R.drawable.ic_settings_black_24dp)
        }

        add(
            R.id.menu_section_three,
            R.id.menu_action_logout,
            Menu.NONE,
            R.string.action_logout
        ).setIcon(R.drawable.ic_logout_black_24dp)

        setGroupCheckable(R.id.menu_section_one, true, true)
        setGroupCheckable(R.id.menu_section_two, true, true)
        setGroupCheckable(R.id.menu_section_three, true, true)
    }
}

internal fun MainActivity.onNavDrawerItemSelected(menuItem: MenuItem) {
    when (menuItem.itemId) {
        R.id.menu_action_chats-> presenter.toChatList()
        R.id.menu_action_create_channel -> presenter.toCreateChannel()
        R.id.menu_action_profile -> presenter.toUserProfile()
        R.id.menu_action_settings -> presenter.toSettings()
        R.id.menu_action_admin_panel -> presenter.toAdminPanel()
        R.id.menu_action_logout -> showLogoutDialog()
    }
}
