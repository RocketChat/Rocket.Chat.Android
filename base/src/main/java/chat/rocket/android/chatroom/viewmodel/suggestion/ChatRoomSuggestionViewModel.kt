package chat.rocket.android.chatroom.viewmodel.suggestion

import chat.rocket.android.widget.autocompletion.model.SuggestionModel

class ChatRoomSuggestionViewModel(text: String,
                                  val fullName: String,
                                  val name: String,
                                  searchList: List<String>) : SuggestionModel(text, searchList, false) {
}