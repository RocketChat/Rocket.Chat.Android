package chat.rocket.android.chatroom.uimodel.suggestion

import chat.rocket.android.suggestions.model.SuggestionModel

class CommandSuggestionUiModel(
    text: String,
    val description: String,
    searchList: List<String>
) : SuggestionModel(text, searchList)