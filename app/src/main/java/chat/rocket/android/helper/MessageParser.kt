package chat.rocket.android.helper

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.Spannable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.text.style.ReplacementSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.PatternsCompat
import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.StrikethroughDelimiterProcessor
import chat.rocket.android.emoji.EmojiParser
import chat.rocket.android.emoji.EmojiRepository
import chat.rocket.android.emoji.EmojiTypefaceSpan
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.util.extensions.openTabbedUrl
import chat.rocket.common.model.SimpleUser
import chat.rocket.core.model.Message
import org.commonmark.Extension
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.StrongEmphasis
import org.commonmark.parser.Parser
import ru.noties.markwon.SpannableBuilder
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.renderer.SpannableMarkdownVisitor
import ru.noties.markwon.tasklist.TaskListExtension
import java.util.*
import javax.inject.Inject

class MessageParser @Inject constructor(
    private val context: Application,
    private val configuration: SpannableConfiguration,
    private val settings: PublicSettings
) {

    /**
     * Render markdown and other rules on message to rich text with spans.
     *
     * @param message The [Message] object we're interested on rendering.
     * @param selfUsername This user username.
     *
     * @return A Spannable with the parsed markdown.
     */
    fun render(message: Message, selfUsername: String? = null): CharSequence {
        var text: String = message.message
        val mentions = mutableListOf<String>()

        val parser = Parser.Builder()
            .extensions(Arrays.asList<Extension>(
                StrikethroughExtension.create(),
                TablesExtension.create(),
                TaskListExtension.create()
            ))
            .customDelimiterProcessor(StrikethroughDelimiterProcessor())
            .build()

        message.mentions?.forEach {
            val mention = getMention(it)
            mentions.add(mention)
            if (it.username != null) {
                text = text.replace("@${it.username}", mention)
            }
        }

        val builder = SpannableBuilder()
        val content = EmojiRepository.shortnameToUnicode(text)
        val parentNode = parser.parse(content)

        parentNode.accept(EmphasisVisitor())
        parentNode.accept(StrongEmphasisVisitor())
        parentNode.accept(MarkdownVisitor(configuration, builder))
        parentNode.accept(LinkVisitor(builder))
        parentNode.accept(EmojiVisitor(context, configuration, builder))

        message.mentions?.let {
            parentNode.accept(MentionVisitor(context, builder, mentions, selfUsername))
        }

        return builder.text()
    }

    private fun getMention(user: SimpleUser): String {
        return if (settings.useRealName()) {
            user.name ?: "@${user.username}"
        } else {
            "@${user.username}"
        }
    }

    class EmphasisVisitor : AbstractVisitor() {

        override fun visit(emphasis: Emphasis) {
            if (emphasis.openingDelimiter == "*" && emphasis.firstChild != null) {
                val child = emphasis.firstChild
                val strongEmphasis = StrongEmphasis()
                strongEmphasis.appendChild(child)
                emphasis.insertBefore(strongEmphasis)
                emphasis.unlink()
            }
        }
    }

    class StrongEmphasisVisitor : AbstractVisitor() {

        override fun visit(strongEmphasis: StrongEmphasis) {
            if (strongEmphasis.openingDelimiter == "__" && strongEmphasis.firstChild != null) {
                val child = strongEmphasis.firstChild
                val emphasis = Emphasis()
                emphasis.appendChild(child)
                strongEmphasis.insertBefore(emphasis)
                strongEmphasis.unlink()
            }
        }
    }

    class MentionVisitor(
        context: Context,
        private val builder: SpannableBuilder,
        private val mentions: List<String>,
        private val currentUser: String?
    ) : AbstractVisitor() {

        private val othersTextColor = ResourcesCompat.getColor(context.resources, R.color.colorAccent, context.theme)
        private val othersBackgroundColor = ResourcesCompat.getColor(context.resources, android.R.color.transparent, context.theme)
        private val myselfTextColor = ResourcesCompat.getColor(context.resources, R.color.colorWhite, context.theme)
        private val myselfBackgroundColor = ResourcesCompat.getColor(context.resources, R.color.colorAccent, context.theme)
        private val padding = context.resources.getDimensionPixelSize(R.dimen.padding_mention).toFloat()
        private val radius = context.resources.getDimensionPixelSize(R.dimen.radius_mention).toFloat()

        override fun visit(document: Document) {
            val text = builder.text()

            var offset = 0
            mentions.forEach {
                val mentionMe = it == "@$currentUser" || it == "@all" || it == "@here"
                offset = text.indexOf(string = it, startIndex = offset, ignoreCase = false)
                while (offset > -1) {
                    val textColor = if (mentionMe) myselfTextColor else othersTextColor
                    val backgroundColor = if (mentionMe) myselfBackgroundColor else othersBackgroundColor
                    val usernameSpan = MentionSpan(backgroundColor, textColor, radius, padding,
                        mentionMe)
                    val end = offset + it.length
                    builder.setSpan(usernameSpan, offset, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    offset = text.indexOf(string = it, startIndex = end, ignoreCase = false)
                }
            }
        }
    }

    class EmojiVisitor(
        private val context: Context,
        configuration: SpannableConfiguration,
        private val builder: SpannableBuilder
    ) : SpannableMarkdownVisitor(configuration, builder) {

        private val emojiSize = context.resources.getDimensionPixelSize(R.dimen.custom_emoji_small)

        override fun visit(document: Document) {
            val spannable = EmojiParser.parse(context, builder.text())
            if (spannable is Spanned) {
                val emojiOneTypefaceSpans = spannable.getSpans(0, spannable.length,
                    EmojiTypefaceSpan::class.java)
                val emojiImageSpans = spannable.getSpans(0, spannable.length, ImageSpan::class.java)

                emojiOneTypefaceSpans.forEach {
                    builder.setSpan(it, spannable.getSpanStart(it), spannable.getSpanEnd(it),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                emojiImageSpans.forEach {
                    it.drawable?.setBounds(0, 0, emojiSize, emojiSize)
                    builder.setSpan(it, spannable.getSpanStart(it), spannable.getSpanEnd(it),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    class MarkdownVisitor(
        configuration: SpannableConfiguration,
        val builder: SpannableBuilder
    ) : SpannableMarkdownVisitor(configuration, builder) {

        /**
         * NOOP
         */
        override fun visit(orderedList: OrderedList) {
            var number = orderedList.startNumber
            val delimiter = orderedList.delimiter
            var node: Node? = orderedList.firstChild
            while (node != null) {
                if (node is ListItem) {
                    newLine()
                    builder.append("$number$delimiter ")
                    super.visitChildren(node)
                    newLine()
                }
                number++
                node = node.next
            }
        }

        private fun newLine() {
            if (builder.isNotEmpty() && '\n' != builder.lastChar()) {
                builder.append('\n')
            }
        }
    }

    class LinkVisitor(private val builder: SpannableBuilder) : AbstractVisitor() {

        override fun visit(document: Document) {
            // Replace all url links to markdown url syntax.
            val matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(builder.text())
            val consumed = mutableListOf<String>()

            while (matcher.find()) {
                val link = matcher.group(0)
                // skip usernames
                if (!link.startsWith("@") && link !in consumed) {
                    builder.setSpan(object : ClickableSpan() {
                        override fun onClick(view: View) {
                            view.openTabbedUrl(link)
                        }
                    }, matcher.start(0), matcher.end(0))
                    consumed.add(link)
                }
            }
        }
    }

    class MentionSpan(
        private val backgroundColor: Int,
        private val textColor: Int,
        private val radius: Float,
        padding: Float,
        referSelf: Boolean
    ) : ReplacementSpan() {

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
