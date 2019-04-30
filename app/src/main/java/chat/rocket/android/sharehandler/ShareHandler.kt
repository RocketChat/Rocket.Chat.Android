package chat.rocket.android.sharehandler

import android.content.Context
import android.content.Intent
import chat.rocket.android.util.extensions.getFileName
import java.io.InputStream


object ShareHandler {

    fun hasShare(): Boolean = hasSharedText() || hasSharedImage()

    fun hasSharedText(): Boolean = sharedText != null
    fun hasSharedImage(): Boolean = files.size > 0

    var sharedText: String? = null

    var files: ArrayList<SharedFile> = arrayListOf()

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
                } else if (type.startsWith("image")) {
                    handleSendImage(intent, context)
                }
            } else if (Intent.ACTION_SEND_MULTIPLE == action) {
                if (type.startsWith("image")) {
                    handleSendImage(intent, context)
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    private fun handleSendImage(intent: Intent, context: Context) {
        intent.clipData?.apply {
            for (pos in 0 until itemCount) {
                val uri = getItemAt(pos).uri

                context.contentResolver.apply {
                    openInputStream(getItemAt(pos).uri)?.let {
                        files.add(SharedFile(it, uri.getFileName(context).orEmpty()))
                    }
                }
            }
        }
    }

    fun getTextAndClear(): String {
        val text = sharedText.orEmpty()
        sharedText = null

        return text
    }

    class SharedFile(var fis: InputStream, var name: String)
}