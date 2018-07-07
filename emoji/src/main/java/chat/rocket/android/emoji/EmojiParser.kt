package chat.rocket.android.emoji

import android.text.SpannableString
import android.text.Spanned

class EmojiParser {

    companion object {
        /**
         * Parses a text string containing unicode characters and/or shortnames to a rendered
         * Spannable.
         *
         * @param text The text to parse
         * @return A rendered Spannable containing any supported emoji.
         */
        fun parse(text: CharSequence): CharSequence {
            val unicodedText = EmojiRepository.shortnameToUnicode(text, true)
            val spannable = SpannableString.valueOf(unicodedText)
            val typeface = EmojiRepository.cachedTypeface
            // Look for groups of emojis, set a EmojiTypefaceSpan with the emojione font.
            val length = spannable.length
            var inEmoji = false
            var emojiStart = 0
            var offset = 0
            while (offset < length) {
                val codepoint = unicodedText.codePointAt(offset)
                val count = Character.charCount(codepoint)
                // Skip control characters.
                if (codepoint == 0x2028) {
                    offset += count
                    continue
                }
                if (codepoint >= 0x200) {
                    if (!inEmoji) {
                        emojiStart = offset
                    }
                    inEmoji = true
                } else {
                    if (inEmoji) {
                        spannable.setSpan(EmojiTypefaceSpan("sans-serif", typeface),
                                emojiStart, offset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    inEmoji = false
                }
                offset += count
                if (offset >= length && inEmoji) {
                    spannable.setSpan(EmojiTypefaceSpan("sans-serif", typeface),
                            emojiStart, offset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            return spannable
        }
    }
}