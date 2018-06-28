package chat.rocket.android.chatroom.ui

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import chat.rocket.android.R
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.util.extensions.content
import com.google.android.material.snackbar.BaseTransientBottomBar
import kotlinx.android.synthetic.main.message_action_bar.view.*
import ru.noties.markwon.Markwon

class ActionSnackbar private constructor(
    parentViewGroup: ViewGroup, content:
    View, contentViewCallback: com.google.android.material.snackbar.ContentViewCallback
) : BaseTransientBottomBar<ActionSnackbar>(parentViewGroup, content, contentViewCallback) {

    companion object {
        fun make(parentViewGroup: ViewGroup, content: String = "", parser: MessageParser): ActionSnackbar {
            val context = parentViewGroup.context
            val view = LayoutInflater.from(context).inflate(R.layout.message_action_bar, parentViewGroup, false)
            val actionSnackbar = ActionSnackbar(parentViewGroup, view, CallbackImpl(view))
            with(view) {
                actionSnackbar.getView().setPadding(0)
                actionSnackbar.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
                actionSnackbar.parser = parser
                actionSnackbar.messageTextView = text_view_action_text
                actionSnackbar.titleTextView = text_view_action_title
                actionSnackbar.cancelView = image_view_action_cancel_quote
                actionSnackbar.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
                val spannable = Markwon.markdown(context, content).trim()
                actionSnackbar.messageTextView.content = spannable
            }
            return actionSnackbar
        }
    }

    lateinit var parser: MessageParser
    lateinit var cancelView: View
    private lateinit var messageTextView: TextView
    private lateinit var titleTextView: TextView

    var text: String = ""
        set(value) {
            val spannable = SpannableStringBuilder.valueOf(value)
            messageTextView.content = spannable
        }

    var title: String = ""
        set(value) {
            val spannable = Markwon.markdown(this.context, value) as Spannable
            titleTextView.content = spannable

        }

    override fun dismiss() {
        super.dismiss()
        text = ""
        title = ""
    }

    class CallbackImpl(val content: View) : com.google.android.material.snackbar.ContentViewCallback {

        override fun animateContentOut(delay: Int, duration: Int) {
            content.scaleY = 1f
            ViewCompat.animate(content)
                .scaleY(0f)
                .setDuration(duration.toLong())
                .startDelay = delay.toLong()
        }

        override fun animateContentIn(delay: Int, duration: Int) {
            content.scaleY = 0f
            ViewCompat.animate(content)
                .scaleY(1f)
                .setDuration(duration.toLong())
                .startDelay = delay.toLong()
        }
    }
}