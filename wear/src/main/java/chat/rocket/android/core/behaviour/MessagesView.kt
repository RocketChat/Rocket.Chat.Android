package chat.rocket.android.core.behaviour

import android.support.annotation.StringRes

interface MessagesView {
    /**
     * Show message given by resource id.
     *
     * @param resId The resource id on strings.xml of the message.
     */
    fun showMessage(@StringRes resId: Int)

    fun showMessage(message: String)

    fun showGenericErrorMessage()
}