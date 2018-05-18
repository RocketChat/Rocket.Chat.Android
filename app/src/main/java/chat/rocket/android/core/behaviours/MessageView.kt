package chat.rocket.android.core.behaviours

import androidx.annotation.StringRes
import chat.rocket.common.util.ifNull

interface MessageView {

    /**
     * Show message given by resource id.
     *
     * @param resId The resource id on strings.xml of the message.
     */
    fun showMessage(@StringRes resId: Int)

    fun showMessage(message: String)

    fun showGenericErrorMessage()
}

fun MessageView.showMessage(ex: Exception) {
    ex.message?.let {
        showMessage(it)
    }.ifNull {
        showGenericErrorMessage()
    }
}