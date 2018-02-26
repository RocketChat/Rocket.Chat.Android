package chat.rocket.android.helper

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Browser
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.text.Layout
import android.text.Spannable
import android.text.Spanned
import android.text.style.*
import android.util.Patterns
import android.view.View
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.BlockQuote
import org.commonmark.node.Text
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableBuilder
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.renderer.SpannableMarkdownVisitor
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

class MessageParser @Inject constructor(val context: Application, private val configuration: SpannableConfiguration) {

    private val parser = Markwon.createParser()
    private val regexUsername = Pattern.compile("([^\\S]|^)+(@[\\w.\\-]+)",
            Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)
    private val selfReferList = listOf("@all", "@here")

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
        val content = text
        val parentNode = parser.parse(toLenientMarkdown(content))
        parentNode.accept(QuoteMessageBodyVisitor(context, configuration, builder))
        quote?.apply {
            var quoteNode = parser.parse("> $senderName $time")
            parentNode.appendChild(quoteNode)
            quoteNode.accept(QuoteMessageSenderVisitor(context, configuration, builder, senderName.length))
            quoteNode = parser.parse("> ${toLenientMarkdown(quote.rawData.message)}")
            quoteNode.accept(QuoteMessageBodyVisitor(context, configuration, builder))
        }
        parentNode.accept(LinkVisitor(builder))
        val result = builder.text()
        applySpans(result, selfUsername)

        return result
    }

    private fun applySpans(text: CharSequence, currentUser: String?) {
        if (text !is Spannable) return
        applyMentionSpans(text, currentUser)
    }

    private fun applyMentionSpans(text: CharSequence, currentUser: String?) {
        val matcher = regexUsername.matcher(text)
        val result = text as Spannable
        while (matcher.find()) {
            val user = matcher.group(2)
            val start = matcher.start(2)
            //TODO: should check if username actually exists prior to applying.
            with(context) {
                val referSelf = when (user) {
                    in selfReferList -> true
                    "@$currentUser" -> true
                    else -> false
                }
                val mentionTextColor: Int
                val mentionBgColor: Int
                if (referSelf) {
                    mentionTextColor = ResourcesCompat.getColor(resources, R.color.white, theme)
                    mentionBgColor = ResourcesCompat.getColor(context.resources,
                            R.color.colorAccent, theme)
                } else {
                    mentionTextColor = ResourcesCompat.getColor(resources, R.color.colorAccent,
                            theme)
                    mentionBgColor = ResourcesCompat.getColor(resources,
                            android.R.color.transparent, theme)
                }

                val padding = resources.getDimensionPixelSize(R.dimen.padding_mention).toFloat()
                val radius = resources.getDimensionPixelSize(R.dimen.radius_mention).toFloat()
                val usernameSpan = MentionSpan(mentionBgColor, mentionTextColor, radius, padding,
                        referSelf)
                result.setSpan(usernameSpan, start, start + user.length, 0)
            }
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
            builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.darkGray)),
                    timeOffsetStart, builder.length())
        }
    }

    class LinkVisitor(private val builder: SpannableBuilder) : AbstractVisitor() {

        override fun visit(text: Text) {
            // Replace all url links to markdown url syntax.
            val matcher = Patterns.WEB_URL.matcher(builder.text())
            val consumed = mutableListOf<String>()

            while (matcher.find()) {
                val link = matcher.group(0)
                // skip usernames
                if (!link.startsWith("@") && link !in consumed) {
                    builder.setSpan(object : ClickableSpan() {
                        override fun onClick(view: View) {
                            val uri = getUri(link)
                            val context = view.context
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Timber.e("Actvity was not found for intent, $intent")
                            }

                        }
                    }, matcher.start(0), matcher.end(0))
                    consumed.add(link)
                }
            }
            visitChildren(text)
        }

        private fun getUri(link: String): Uri {
            val uri = Uri.parse(link)
            if (uri.scheme == null) {
                return Uri.parse("http://$link")
            }
            return uri
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

            val padding = context.resources.getDimensionPixelSize(R.dimen.padding_quote)
            builder.setSpan(QuoteMarginSpan(context.getDrawable(R.drawable.quote), padding), length,
                    builder.length())
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

    class MentionSpan(private val backgroundColor: Int,
                      private val textColor: Int,
                      private val radius: Float,
                      padding: Float,
                      referSelf: Boolean) : ReplacementSpan() {
        private val padding: Float = if (referSelf) padding else 0F

        override fun getSize(paint: Paint,
                             text: CharSequence,
                             start: Int,
                             end: Int,
                             fm: Paint.FontMetricsInt?): Int {
            return (padding + paint.measureText(text.subSequence(start, end).toString()) + padding).toInt()
        }

        override fun draw(canvas: Canvas,
                          text: CharSequence,
                          start: Int,
                          end: Int,
                          x: Float,
                          top: Int,
                          y: Int,
                          bottom: Int,
                          paint: Paint) {
            val length = paint.measureText(text.subSequence(start, end).toString())
            val rect = RectF(x, top.toFloat(), x + length + padding * 2,
                    bottom.toFloat())
            paint.setColor(backgroundColor)
            canvas.drawRoundRect(rect, radius, radius, paint)
            paint.setColor(textColor)
            canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
        }

    }
}