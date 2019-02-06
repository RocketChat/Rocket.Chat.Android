package chat.rocket.android.chatdetails.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import chat.rocket.android.chatdetails.domain.ChatDetails
import chat.rocket.android.db.ChatRoomDao

class ChatDetailsViewModel(private val chatRoomDao: ChatRoomDao): ViewModel() {

    fun getDetails(chatRoomId: String): LiveData<ChatDetails> {
        return Transformations.switchMap(chatRoomDao.get(chatRoomId)) { room ->
            val entity = room.chatRoom
            val data: MutableLiveData<ChatDetails> = MutableLiveData()

            data.value = ChatDetails(
                entity.name,
                entity.fullname,
                entity.type,
                entity.topic,
                entity.announcement,
                entity.description
            )

            return@switchMap data
        }
    }
}