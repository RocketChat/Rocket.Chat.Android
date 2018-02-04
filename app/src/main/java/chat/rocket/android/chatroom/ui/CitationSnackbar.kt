package chat.rocket.android.chatroom.ui

import android.graphics.drawable.Drawable
import android.support.design.widget.BaseTransientBottomBar
import android.support.v4.view.ViewCompat
import android.text.Spannable
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.util.content
import ru.noties.markwon.Markwon

class CitationSnackbar : BaseTransientBottomBar<CitationSnackbar> {

    companion object {
        fun make(parentViewGroup: ViewGroup, content: String): CitationSnackbar {
            val context = parentViewGroup.context
            val view = LayoutInflater.from(context).inflate(R.layout.quote_bar, parentViewGroup, false)
            val citationSnackbar = CitationSnackbar(parentViewGroup, view, CallbackImpl(view))
            citationSnackbar.textView = view.findViewById(R.id.text_view_quote) as TextView
            citationSnackbar.cancelView = view.findViewById(R.id.image_view_cancel_quote) as ImageView
            citationSnackbar.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            val spannable = SpannableString(content)
            citationSnackbar.marginDrawable = context.getDrawable(R.drawable.quote)
            spannable.setSpan(MessageParser.QuoteMarginSpan(citationSnackbar.marginDrawable, 10), 0, content.length, 0)
            citationSnackbar.textView.content = spannable
            return citationSnackbar
        }
    }

    lateinit var cancelView: View
    private lateinit var textView: TextView
    private lateinit var marginDrawable: Drawable

    var text: String = ""
        set(value) {
            val spannable = Markwon.markdown(this.context, value) as Spannable
            spannable.setSpan(MessageParser.QuoteMarginSpan(marginDrawable, 10), 0, spannable.length, 0)
            textView.content = spannable
        }

    override fun dismiss() {
        super.dismiss()
        text = ""
    }

    private constructor(parentViewGroup: ViewGroup, content: View, contentViewCallback: BaseTransientBottomBar.ContentViewCallback) :
            super(parentViewGroup, content, contentViewCallback)

    class CallbackImpl(val content: View) : BaseTransientBottomBar.ContentViewCallback {
        override fun animateContentOut(delay: Int, duration: Int) {
            ViewCompat.setScaleY(content, 1f)
            ViewCompat.animate(content)
                    .scaleY(0f)
                    .setDuration(duration.toLong())
                    .setStartDelay(delay.toLong())
        }

        override fun animateContentIn(delay: Int, duration: Int) {
            ViewCompat.setScaleY(content, 0f)
            ViewCompat.animate(content)
                    .scaleY(1f).setDuration(duration.toLong())
                    .setStartDelay(delay.toLong())
        }
    }
}