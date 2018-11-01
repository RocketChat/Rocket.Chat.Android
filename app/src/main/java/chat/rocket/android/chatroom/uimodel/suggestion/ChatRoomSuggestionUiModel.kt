package chat.rocket.android.chatroom.uimodel.suggestion

import chat.rocket.android.suggestions.model.SuggestionModel

class ChatRoomSuggestionUiModel(
    text: String,
    val fullName: String,
    val name: String,
    searchList: List<String>
) : SuggestionModel(text, searchList, false)
