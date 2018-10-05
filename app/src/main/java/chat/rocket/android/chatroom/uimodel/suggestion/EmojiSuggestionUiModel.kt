package chat.rocket.android.chatroom.uimodel.suggestion

import chat.rocket.android.emoji.Emoji
import chat.rocket.android.suggestions.model.SuggestionModel
import chat.rocket.common.model.UserStatus

class EmojiSuggestionUiModel(
    text: String,
    pinned: Boolean = false,
    val emoji: Emoji,
    searchList: List<String>
) : SuggestionModel(text, searchList, pinned) {

    override fun toString(): String {
        return "EmojiSuggestionUiModel(text='$text', searchList='$searchList', pinned=$pinned)"
    }
}
