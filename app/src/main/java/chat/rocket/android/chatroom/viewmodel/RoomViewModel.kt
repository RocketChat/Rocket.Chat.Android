package chat.rocket.android.chatroom.viewmodel

import chat.rocket.android.widget.autocompletion.model.SuggestionModel

class RoomViewModel(text: String,
                    val fullName: String,
                    val name: String,
                    searchList: List<String>) : SuggestionModel(text, searchList, false) {
}