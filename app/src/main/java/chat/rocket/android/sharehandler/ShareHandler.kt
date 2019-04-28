package chat.rocket.android.sharehandler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.content.pm.ResolveInfo
import android.content.pm.PackageManager


object ShareHandler {

    fun hasShare(): Boolean = hasSharedText() || hasSharedImage()

    fun hasSharedText(): Boolean = sharedText != null
    fun hasSharedImage(): Boolean = sharedImage != null

    private var sharedText: String? = null
    private var sharedImage: Uri? = null

    // TODO request permission.
    fun handle(intent: Intent?, context: Context) {
        intent?.let {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val action = it.action
            val type = it.type

            if (type.isNullOrEmpty() || action.isNullOrEmpty())
                return@let

            if (Intent.ACTION_SEND == action) {
                if ("text/plain" == type) {
                    handleSendText(intent)
                } else if (type.startsWith("image/")) {
                    handleSendImage(intent, context)
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    private fun handleSendImage(intent: Intent, context: Context) {
        sharedImage = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?

//        context.grantUriPermission("chat.rocket.android.dev", sharedImage, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    fun getTextAndClear(): String {
        val text = sharedText.orEmpty()
        sharedText = null

        return text
    }

    fun getImageAndClear(): Uri? {
        val uri = sharedImage

        sharedImage = null
        return uri
    }

    fun clearShare() {
        sharedText = null
        sharedImage = null
    }

}