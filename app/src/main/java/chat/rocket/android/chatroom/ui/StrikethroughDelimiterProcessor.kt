package chat.rocket.android.chatroom.ui

import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.delimiter.DelimiterProcessor
import org.commonmark.parser.delimiter.DelimiterRun

class StrikethroughDelimiterProcessor : DelimiterProcessor {

    override fun getOpeningCharacter(): Char {
        return '~'
    }

    override fun getClosingCharacter(): Char {
        return '~'
    }

    override fun getMinLength(): Int {
        return 1
    }

    override fun getDelimiterUse(opener: DelimiterRun, closer: DelimiterRun): Int {
        return if (opener.length() >= 2 && closer.length() >= 2) {
            // Use exactly two delimiters even if we have more, and don't care about internal openers/closers.
            2
        } else {
            1
        }
    }

    override fun process(opener: Text, closer: Text, delimiterCount: Int) {
        // Wrap nodes between delimiters in strikethrough.
        val strikethrough = Strikethrough()

        var tmp: Node? = opener.next
        while (tmp != null && tmp !== closer) {
            val next = tmp.next
            strikethrough.appendChild(tmp)
            tmp = next
        }

        opener.insertAfter(strikethrough)
    }
}
