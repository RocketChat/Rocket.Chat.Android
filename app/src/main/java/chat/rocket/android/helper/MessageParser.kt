package chat.rocket.android.helper

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.provider.Browser
import android.support.v4.content.res.ResourcesCompat
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ReplacementSpan
import android.util.Patterns
import android.view.View
import chat.rocket.android.R
import chat.rocket.android.widget.emoji.EmojiParser
import chat.rocket.android.widget.emoji.EmojiRepository
import chat.rocket.android.widget.emoji.EmojiTypefaceSpan
import chat.rocket.common.model.SimpleUser
import chat.rocket.core.model.Message
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Document
import org.commonmark.node.Text
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableBuilder
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.renderer.SpannableMarkdownVisitor
import timber.log.Timber
import javax.inject.Inject

class MessageParser @Inject constructor(val context: Application, private val configuration: SpannableConfiguration) {

    private val parser = Markwon.createParser()

    /**
     * Render a markdown text message to Spannable.
     *
     * @param message The [Message] object we're interested on rendering.
     * @param selfUsername This user username.
     *
     * @return A Spannable with the parsed markdown.
     */
    fun renderMarkdown(message: Message, selfUsername: String? = null): CharSequence {
        val text = message.message
        val builder = SpannableBuilder()
        val content = EmojiRepository.shortnameToUnicode(text, true)
        val parentNode = parser.parse(toLenientMarkdown(content))
        parentNode.accept(SpannableMarkdownVisitor(configuration, builder))
        parentNode.accept(LinkVisitor(builder))
        parentNode.accept(EmojiVisitor(configuration, builder))
        message.mentions?.let {
            parentNode.accept(MentionVisitor(context, builder, it, selfUsername))
        }

        return builder.text()
    }

    // Convert to a lenient markdown consistent with Rocket.Chat web markdown instead of the official specs.
    private fun toLenientMarkdown(text: String): String {
        return text.trim().replace("\\*(.+)\\*".toRegex()) { "**${it.groupValues[1].trim()}**" }
                .replace("\\~(.+)\\~".toRegex()) { "~~${it.groupValues[1].trim()}~~" }
                .replace("\\_(.+)\\_".toRegex()) { "_${it.groupValues[1].trim()}_" }
    }

    class MentionVisitor(context: Context,
                         private val builder: SpannableBuilder,
                         private val mentions: List<SimpleUser>,
                         private val currentUser: String?) : AbstractVisitor() {
        private val othersTextColor = ResourcesCompat.getColor(context.resources, R.color.colorAccent, context.theme)
        private val othersBackgroundColor = ResourcesCompat.getColor(context.resources, android.R.color.transparent, context.theme)
        private val myselfTextColor = ResourcesCompat.getColor(context.resources, R.color.white, context.theme)
        private val myselfBackgroundColor = ResourcesCompat.getColor(context.resources, R.color.colorAccent, context.theme)
        private val mentionPadding = context.resources.getDimensionPixelSize(R.dimen.padding_mention).toFloat()
        private val mentionRadius = context.resources.getDimensionPixelSize(R.dimen.radius_mention).toFloat()
        override fun visit(t: Text) {
            val text = t.literal
            val mentionsList = mentions.map { it.username }.toMutableList()
            mentionsList.add("all")
            mentionsList.add("here")

            mentionsList.toList().forEach {
                if (it != null) {
                    val mentionMe = it == currentUser || it == "all" || it == "here"
                    var offset = text.indexOf("@$it", 0, true)
                    while (offset > -1) {
                        val textColor = if (mentionMe) myselfTextColor else othersTextColor
                        val backgroundColor = if (mentionMe) myselfBackgroundColor else othersBackgroundColor
                        val usernameSpan = MentionSpan(backgroundColor, textColor, mentionRadius, mentionPadding,
                                mentionMe)
                        // Add 1 to end offset to include the @.
                        val end = offset + it.length + 1
                        builder.setSpan(usernameSpan, offset, end, 0)
                        offset = text.indexOf("@$it", end, true)
                    }
                }
            }
        }
    }

    class EmojiVisitor(configuration: SpannableConfiguration, private val builder: SpannableBuilder)
        : SpannableMarkdownVisitor(configuration, builder) {
        override fun visit(document: Document) {
            val spannable = EmojiParser.parse(builder.text())
            if (spannable is Spanned) {
                val spans = spannable.getSpans(0, spannable.length, EmojiTypefaceSpan::class.java)
                spans.forEach {
                    builder.setSpan(it, spannable.getSpanStart(it), spannable.getSpanEnd(it), 0)
                }
            }
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
                            with (view) {
                                val tabsbuilder = CustomTabsIntent.Builder()
                                tabsbuilder.setToolbarColor(ResourcesCompat.getColor(context.resources, R.color.colorPrimary, context.theme))
                                val customTabsIntent = tabsbuilder.build()
                                customTabsIntent.launchUrl(context, getUri(link))
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
            val rect = RectF(x, top.toFloat(), x + length + padding * 2, bottom.toFloat())
            paint.color = backgroundColor
            canvas.drawRoundRect(rect, radius, radius, paint)
            paint.color = textColor
            canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
        }
    }
}