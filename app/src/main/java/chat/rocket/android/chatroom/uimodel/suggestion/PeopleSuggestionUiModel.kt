package chat.rocket.android.chatroom.uimodel.suggestion

import chat.rocket.android.suggestions.model.SuggestionModel
import chat.rocket.common.model.UserStatus

class PeopleSuggestionUiModel(
    val imageUri: String?,
    text: String,
    val username: String,
    val name: String,
    val status: UserStatus?,
    pinned: Boolean = false,
    searchList: List<String>
) : SuggestionModel(text, searchList, pinned) {

    override fun toString(): String {
        return "PeopleSuggestionUiModel(imageUri='$imageUri', username='$username', name='$name', status=$status, pinned=$pinned)"
    }
}