package chat.rocket.android.chatroom.ui

import android.support.design.widget.BaseTransientBottomBar
import android.support.v4.view.ViewCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.util.extensions.content
import ru.noties.markwon.Markwon

class ActionSnackbar : BaseTransientBottomBar<ActionSnackbar> {

    companion object {
        fun make(parentViewGroup: ViewGroup, content: String = "", parser: MessageParser): ActionSnackbar {
            val context = parentViewGroup.context
            val view = LayoutInflater.from(context).inflate(R.layout.message_action_bar, parentViewGroup, false)
            val actionSnackbar = ActionSnackbar(parentViewGroup, view, CallbackImpl(view))
            actionSnackbar.parser = parser
            actionSnackbar.messageTextView = view.findViewById(R.id.text_view_action_text) as TextView
            actionSnackbar.titleTextView = view.findViewById(R.id.text_view_action_title) as TextView
            actionSnackbar.cancelView = view.findViewById(R.id.image_view_action_cancel_quote) as ImageView
            actionSnackbar.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            val spannable = Markwon.markdown(context, content).trim()
            actionSnackbar.messageTextView.content = spannable
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

    private constructor(parentViewGroup: ViewGroup, content: View, contentViewCallback: BaseTransientBottomBar.ContentViewCallback) :
            super(parentViewGroup, content, contentViewCallback)

    class CallbackImpl(val content: View) : BaseTransientBottomBar.ContentViewCallback {

        override fun animateContentOut(delay: Int, duration: Int) {
            ViewCompat.setScaleY(content, 1f)
            ViewCompat.animate(content)
                    .scaleY(0f)
                    .setDuration(duration.toLong())
                    .startDelay = delay.toLong()
        }

        override fun animateContentIn(delay: Int, duration: Int) {
            ViewCompat.setScaleY(content, 0f)
            ViewCompat.animate(content)
                    .scaleY(1f)
                    .setDuration(duration.toLong())
                    .startDelay = delay.toLong()
        }
    }
}