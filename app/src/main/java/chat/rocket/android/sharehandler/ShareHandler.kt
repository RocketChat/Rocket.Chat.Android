package chat.rocket.android.sharehandler

import android.content.Intent
import android.net.Uri
import android.os.Parcelable

object ShareHandler {

    fun hasShare(): Boolean = hasSharedText() || hasSharedImage()

    fun hasSharedText(): Boolean = sharedText != null
    fun hasSharedImage(): Boolean = sharedImage != null

    private var sharedText: String? = null
    private var sharedImage: Uri? = null


    fun handle(intent: Intent?) {
        intent?.let {
            val action = it.action
            val type = it.type

            if (type.isNullOrEmpty() || action.isNullOrEmpty())
                return@let

            if (Intent.ACTION_SEND == action) {
                if ("text/plain" == type) {
                    handleSendText(intent)
                } else if (type.startsWith("image/")) {
                    handleSendImage(intent)
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    private fun handleSendImage(intent: Intent) {
        sharedImage = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?
    }

    fun getTextAndClear(): String {
        val text = sharedText.orEmpty()
        sharedText = null

        return text
    }

    fun clearShare() {
        sharedText = null
        sharedImage = null
    }

}