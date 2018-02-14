package chat.rocket.android.helper

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.*
import android.view.View
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import org.commonmark.node.BlockQuote
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableBuilder
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.renderer.SpannableMarkdownVisitor
import java.util.regex.Pattern
import javax.inject.Inject

class MessageParser @Inject constructor(val context: Application, private val configuration: SpannableConfiguration) {

    private val parser = Markwon.createParser()
    private val regexUsername = Pattern.compile("([^\\S]|^)+(@[\\w.]+)",
            Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)
    private val regexLink = Pattern.compile("(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&/=]*)",
            Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)

    /**
     * Render a markdown text message to Spannable.
     *
     * @param text The text message containing markdown syntax.
     * @param quote An optional message to be quoted either by a quote or reply action.
     * @param urls A list of urls to convert to markdown link syntax.
     *
     * @return A Spannable with the parsed markdown.
     */
    fun renderMarkdown(text: String, quote: MessageViewModel? = null, selfUsername: String? = null): CharSequence {
        val builder = SpannableBuilder()
        var content: String = text

        // Replace all url links to markdown url syntax.
        val matcher = regexLink.matcher(content)
        val consumed = mutableListOf<String>()
        while (matcher.find()) {
            val link = matcher.group(0)
            // skip usernames
            if (!link.startsWith("@") && !consumed.contains(link)) {
                content = content.replace(link, "[$link]($link)")
                consumed.add(link)
            }
        }

        val parentNode = parser.parse(toLenientMarkdown(content))
        parentNode.accept(QuoteMessageBodyVisitor(context, configuration, builder))
        quote?.apply {
            var quoteNode = parser.parse("> $senderName $time")
            parentNode.appendChild(quoteNode)
            quoteNode.accept(QuoteMessageSenderVisitor(context, configuration, builder, senderName.length))
            quoteNode = parser.parse("> ${toLenientMarkdown(quote.getOriginalMessage())}")
            quoteNode.accept(QuoteMessageBodyVisitor(context, configuration, builder))
        }

        val result = builder.text()
        applySpans(result, selfUsername)
        return result
    }

    private fun applySpans(text: CharSequence, currentUser: String?) {
        val matcher = regexUsername.matcher(text)
        val result = text as Spannable
        while (matcher.find()) {
            val user = matcher.group(2)
            val start = matcher.start(2)
            //TODO: should check if username actually exists prior to applying.
            val linkColor = context.resources.getColor(R.color.linkTextColor)
            val linkBackgroundColor = context.resources.getColor(R.color.linkBackgroundColor)
            val referSelf = currentUser != null && "@$currentUser" == user
            val usernameSpan = UsernameClickableSpan(linkBackgroundColor, linkColor, referSelf)
            result.setSpan(usernameSpan, start, start + user.length, 0)
        }
    }

    /**
     * Convert to a lenient markdown consistent with Rocket.Chat web markdown instead of the official specs.
     */
    private fun toLenientMarkdown(text: String): String {
        return text.trim().replace("\\*(.+)\\*".toRegex()) { "**${it.groupValues[1].trim()}**" }
                .replace("\\~(.+)\\~".toRegex()) { "~~${it.groupValues[1].trim()}~~" }
                .replace("\\_(.+)\\_".toRegex()) { "_${it.groupValues[1].trim()}_" }
    }

    class QuoteMessageSenderVisitor(private val context: Context,
                                    configuration: SpannableConfiguration,
                                    private val builder: SpannableBuilder,
                                    private val senderNameLength: Int) : SpannableMarkdownVisitor(configuration, builder) {

        override fun visit(blockQuote: BlockQuote) {

            // mark current length
            val length = builder.length()

            // pass to super to apply markdown
            super.visit(blockQuote)

            val res = context.resources
            val timeOffsetStart = length + senderNameLength + 1
            builder.setSpan(QuoteMarginSpan(context.getDrawable(R.drawable.quote), 10), length, builder.length())
            builder.setSpan(StyleSpan(Typeface.BOLD), length, length + senderNameLength)
            builder.setSpan(ForegroundColorSpan(Color.BLACK), length, builder.length())
            // set time spans
            builder.setSpan(AbsoluteSizeSpan(res.getDimensionPixelSize(R.dimen.message_time_text_size)),
                    timeOffsetStart, builder.length())
            builder.setSpan(ForegroundColorSpan(res.getColor(R.color.darkGray)),
                    timeOffsetStart, builder.length())
        }
    }

    class QuoteMessageBodyVisitor(private val context: Context,
                                  configuration: SpannableConfiguration,
                                  private val builder: SpannableBuilder) : SpannableMarkdownVisitor(configuration, builder) {

        override fun visit(blockQuote: BlockQuote) {

            // mark current length
            val length = builder.length()

            // pass to super to apply markdown
            super.visit(blockQuote)

            builder.setSpan(QuoteMarginSpan(context.getDrawable(R.drawable.quote), 10), length, builder.length())
        }
    }

    class QuoteMarginSpan(quoteDrawable: Drawable, private var pad: Int) : LeadingMarginSpan, LineHeightSpan {
        private val drawable: Drawable = quoteDrawable

        override fun getLeadingMargin(first: Boolean): Int {
            return drawable.intrinsicWidth + pad
        }

        override fun drawLeadingMargin(c: Canvas, p: Paint, x: Int, dir: Int,
                                       top: Int, baseline: Int, bottom: Int,
                                       text: CharSequence, start: Int, end: Int,
                                       first: Boolean, layout: Layout) {
            val st = (text as Spanned).getSpanStart(this)
            val ix = x
            val itop = layout.getLineTop(layout.getLineForOffset(st))
            val dw = drawable.intrinsicWidth
            val dh = drawable.intrinsicHeight
            // XXX What to do about Paint?
            drawable.setBounds(ix, itop, ix + dw, itop + layout.height)
            drawable.draw(c)
        }

        override fun chooseHeight(text: CharSequence, start: Int, end: Int,
                                  spanstartv: Int, v: Int,
                                  fm: Paint.FontMetricsInt) {
            if (end == (text as Spanned).getSpanEnd(this)) {
                val ht = drawable.intrinsicHeight
                var need = ht - (v + fm.descent - fm.ascent - spanstartv)
                if (need > 0)
                    fm.descent += need
                need = ht - (v + fm.bottom - fm.top - spanstartv)
                if (need > 0)
                    fm.bottom += need
            }
        }
    }

    class UsernameClickableSpan(private val linkBackgroundColor: Int,
                                private val linkTextColor: Int,
                                private val referSelf: Boolean) : ClickableSpan() {

        override fun onClick(widget: View) {
            //TODO: Implement action when clicking on username, like showing user profile.
        }

        override fun updateDrawState(ds: TextPaint) {
            if (referSelf) {
                ds.color = Color.WHITE
                ds.typeface = Typeface.DEFAULT_BOLD
                ds.bgColor = linkTextColor
            } else {
                ds.color = linkTextColor
                ds.bgColor = linkBackgroundColor
            }
            ds.isUnderlineText = false
        }

    }
}