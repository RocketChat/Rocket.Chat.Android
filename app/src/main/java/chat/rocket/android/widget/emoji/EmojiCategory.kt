package chat.rocket.android.widget.emoji

import android.text.SpannableString
import android.text.Spanned

enum class EmojiCategory {
    RECENTS {
        override fun icon() = getTextIconFor("\uD83D\uDD58")
    },
    PEOPLE() {
        override fun icon() = getTextIconFor("\uD83D\uDE00")
    },
    NATURE {
        override fun icon() = getTextIconFor("\uD83D\uDC3B")
    },
    FOOD {
        override fun icon() = getTextIconFor("\uD83C\uDF4E")
    },
    ACTIVITY {
        override fun icon() = getTextIconFor("\uD83D\uDEB4")
    },
    TRAVEL {
        override fun icon() = getTextIconFor("\uD83C\uDFD9️")
    },
    OBJECTS {
        override fun icon() = getTextIconFor("\uD83D\uDD2A")
    },
    SYMBOLS {
        override fun icon() = getTextIconFor("⚛")
    },
    FLAGS {
        override fun icon() = getTextIconFor("\uD83D\uDEA9")
    };

    abstract fun icon(): CharSequence

    protected fun getTextIconFor(text: String): CharSequence {
        val span = EmojiTypefaceSpan("sans-serif", EmojiLoader.cachedTypeface)
        return SpannableString.valueOf(text).apply {
            setSpan(span, 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }
    }
}