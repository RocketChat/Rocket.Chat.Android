package chat.rocket.android.settings.presentation

import chat.rocket.android.core.behaviours.MessageView

interface SettingsView:  MessageView {

    fun showDownloadDialog(error : String?)
}

