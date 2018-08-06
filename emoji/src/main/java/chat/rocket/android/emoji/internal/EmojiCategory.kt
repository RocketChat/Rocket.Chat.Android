package chat.rocket.android.emoji.internal

import android.text.SpannableString
import android.text.Spanned
import androidx.annotation.DrawableRes
import chat.rocket.android.emoji.EmojiRepository
import chat.rocket.android.emoji.EmojiTypefaceSpan
import chat.rocket.android.emoji.R

enum class EmojiCategory {
    RECENTS {
        override fun resourceIcon() = R.drawable.ic_emoji_recents

        override fun textIcon() = getTextIconFor("\uD83D\uDD58")
    },
    CUSTOM {
        override fun resourceIcon() = R.drawable.ic_emoji_custom

        override fun textIcon() = getTextIconFor("\uD83D\uDD58")
    },
    PEOPLE() {
        override fun resourceIcon() = R.drawable.ic_emoji_people

        override fun textIcon() = getTextIconFor("\uD83D\uDE00")
    },
    NATURE {
        override fun resourceIcon() = R.drawable.ic_emoji_nature

        override fun textIcon() = getTextIconFor("\uD83D\uDC3B")
    },
    FOOD {
        override fun resourceIcon() = R.drawable.ic_emoji_food

        override fun textIcon() = getTextIconFor("\uD83C\uDF4E")
    },
    ACTIVITY {
        override fun resourceIcon() = R.drawable.ic_emoji_activity

        override fun textIcon() = getTextIconFor("\uD83D\uDEB4")
    },
    TRAVEL {
        override fun resourceIcon() = R.drawable.ic_emoji_travel

        override fun textIcon() = getTextIconFor("\uD83C\uDFD9️")
    },
    OBJECTS {
        override fun resourceIcon() = R.drawable.ic_emoji_objects

        override fun textIcon() = getTextIconFor("\uD83D\uDD2A")
    },
    SYMBOLS {
        override fun resourceIcon() = R.drawable.ic_emoji_symbols

        override fun textIcon() = getTextIconFor("⚛")
    },
    FLAGS {
        override fun resourceIcon() = R.drawable.ic_emoji_flags

        override fun textIcon() = getTextIconFor("\uD83D\uDEA9")
    };

    abstract fun textIcon(): CharSequence

    @DrawableRes
    abstract fun resourceIcon(): Int

    protected fun getTextIconFor(text: String): CharSequence {
        val span = EmojiTypefaceSpan("sans-serif", EmojiRepository.cachedTypeface)
        return SpannableString.valueOf(text).apply {
            setSpan(span, 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }
    }
}
