package chat.rocket.android.widget.emoji

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
            val spannableString = SpannableString.valueOf(unicodedText)
            // Look for groups of emojis, set a CustomTypefaceSpan with the emojione font
            val length = spannableString.length
            var inEmoji = false
            var emojiStart = 0
            var offset = 0
            while (offset < length) {
                val codepoint = unicodedText.codePointAt(offset)
                val count = Character.charCount(codepoint)
                if (codepoint >= 0x200) {
                    if (!inEmoji) {
                        emojiStart = offset
                    }
                    inEmoji = true
                } else {
                    if (inEmoji) {
                        spannableString.setSpan(EmojiTypefaceSpan("sans-serif", EmojiRepository.cachedTypeface),
                                emojiStart, offset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    inEmoji = false
                }
                offset += count
                if (offset >= length && inEmoji) {
                    spannableString.setSpan(EmojiTypefaceSpan("sans-serif", EmojiRepository.cachedTypeface),
                            emojiStart, offset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            return spannableString
        }
    }
}