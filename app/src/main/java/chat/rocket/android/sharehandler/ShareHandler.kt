package chat.rocket.android.sharehandler

import android.content.Context
import android.content.Intent
import androidx.core.net.toFile
import chat.rocket.android.util.extensions.getFileName
import chat.rocket.android.util.extensions.getFileSize
import chat.rocket.android.util.extensions.getMimeType
import java.io.InputStream


object ShareHandler {

    fun hasShare(): Boolean = hasSharedText() || hasSharedFile()

    fun hasSharedText(): Boolean = sharedText != null
    fun hasSharedFile(): Boolean = files.size > 0

    var sharedText: String? = null

    var files: ArrayList<SharedFile> = arrayListOf()

    fun handle(intent: Intent?, context: Context) {
        clearAll()

        intent?.let {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val action = it.action
            val type = it.type

            if (type.isNullOrEmpty() || action.isNullOrEmpty())
                return@let

            if ("text/plain" == type) {
                handleSendText(intent)
            } else {

                // TODO can't share if user were in fragment rather than ChatRoomsFragment
                // TODO request permission
                intent.clipData?.let { data ->
                    if (data.itemCount > 0) {
                        loadFiles(intent, context)
                    }
                }
            }
        }
    }

    private fun clearAll() {
        files.clear()
        sharedText = null
    }

    private fun handleSendText(intent: Intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
    }

    private fun loadFiles(intent: Intent, context: Context) {
        intent.clipData?.apply {
            for (pos in 0 until itemCount) {
                val uri = getItemAt(pos).uri

                context.contentResolver.apply {
                    openInputStream(getItemAt(pos).uri)?.let {
                        files.add(SharedFile(
                            it,
                            uri.toFile().name,
                            uri.getMimeType(context), // TODO some mime types missing and causing crash.
                            uri.getFileSize(context)
                        ))
                    }
                }
            }
        }
    }

    fun getFilesAsString(): Array<String> {
        return Array(files.size) {
            return@Array files[it].name
        }
    }

    fun getTextAndClear(): String {
        val text = sharedText.orEmpty()
        sharedText = null

        return text
    }

    fun getText(): String {
        return sharedText.orEmpty()
    }

    fun clear() {
        files.clear()
        sharedText = null
    }

    class SharedFile(var fis: InputStream, var name: String, val mimeType: String, val size: Int, var send: Boolean = true)
}