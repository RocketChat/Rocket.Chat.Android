package chat.rocket.android.chatroom.uimodel.suggestion

import chat.rocket.android.widget.autocompletion.model.SuggestionModel

class ChatRoomSuggestionUiModel(text: String,
                                val fullName: String,
                                val name: String,
                                searchList: List<String>) : SuggestionModel(text, searchList, false) {
}