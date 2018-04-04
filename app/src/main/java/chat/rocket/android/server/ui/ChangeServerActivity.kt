package chat.rocket.android.server.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.server.presentation.ChangeServerPresenter
import chat.rocket.android.server.presentation.ChangeServerView
import chat.rocket.android.util.extensions.showToast
import dagger.android.AndroidInjection
import javax.inject.Inject

class ChangeServerActivity : AppCompatActivity(), ChangeServerView {
    @Inject lateinit var presenter: ChangeServerPresenter

    var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        val serverUrl: String? = intent.getStringExtra(INTENT_SERVER_URL)
        presenter.loadServer(serverUrl)
    }

    override fun showInvalidCredentials() {
        showToast("Missing credentials for this server")
    }

    override fun showProgress() {
        progress = ProgressDialog.show(this, "Rocket.Chat", "Changing Server")
    }

    override fun hideProgress() {
        progress?.dismiss()
    }
}

private const val INTENT_SERVER_URL = "INTENT_SERVER_URL"
private const val INTENT_CHAT_ROOM_NAME = "INTENT_CHAT_ROOM_NAME"
private const val INTENT_CHAT_ROOM_TYPE = "INTENT_CHAT_ROOM_TYPE"

fun Context.changeServerIntent(serverUrl: String?): Intent {
    return Intent(this, ChangeServerActivity::class.java).apply {
        serverUrl?.let { url ->
            putExtra(INTENT_SERVER_URL, url)
        }
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
}