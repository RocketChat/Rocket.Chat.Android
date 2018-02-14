package chat.rocket.android.helper

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.widget.TextView
import chat.rocket.android.util.extensions.ifEmpty

object TextHelper {

    /**
     * Adds clickable link(s) to a TextView.
     *
     * @param textView The TextView to add clickable links.
     * @param links The TextView content(s) to act as clickable links.
     * @param clickableSpans The ClickableSpan(s) to handle the onClick listener.
     */
    fun addLink(textView: TextView, links: Array<String>, clickableSpans: Array<ClickableSpan>) {
        val spannableString = SpannableString(textView.text)
        for (i in links.indices) {
            val clickableSpan = clickableSpans[i]
            val link = links[i]

            val startIndexOfLink = textView.text.indexOf(link)
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    /**
     * Returns the first character from a string.
     *
     * @param string The string to get its first character.
     * @return The first character from a string.
     */
    fun getFirstCharacter(string: String): String {
        string.ifEmpty("?")
        return string.substring(0, 1).toUpperCase()
    }
}