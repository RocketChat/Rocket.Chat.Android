package chat.rocket.android.chatroom.viewmodel.suggestion

import chat.rocket.android.widget.autocompletion.model.SuggestionModel

class CommandSuggestionViewModel(text: String,
                                 val description: String,
                                 searchList: List<String>) : SuggestionModel(text, searchList)