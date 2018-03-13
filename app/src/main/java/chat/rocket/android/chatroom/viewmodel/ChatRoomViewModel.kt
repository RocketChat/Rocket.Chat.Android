package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.widget.autocompletion.model.SuggestionModel

class ChatRoomViewModel(text: String,
                        val fullName: String,
                        val name: String,
                        searchList: List<String>) : SuggestionModel(text, searchList, false) {
}