package chat.rocket.android.main.presentation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import chat.rocket.core.model.ChatRoom

class ChatRoomsViewModel : ViewModel() {
    private val chatRoomsLiveData = MutableLiveData<List<ChatRoom>>()
    private val selectedChatRoom = MutableLiveData<Pair<ChatRoom, Any>>()

    fun setChatRooms(chatRooms: List<ChatRoom>) {
        chatRoomsLiveData.setValue(chatRooms)
    }

    fun getChatRooms(): LiveData<List<ChatRoom>> = chatRoomsLiveData

    fun selectChatRoom(chatRoom: ChatRoom, content: Any?) {
        content?.let {
            selectedChatRoom.value = Pair(chatRoom, it)
        }
    }

    fun getSelectedChatRoom(): LiveData<Pair<ChatRoom, Any>> = selectedChatRoom

}