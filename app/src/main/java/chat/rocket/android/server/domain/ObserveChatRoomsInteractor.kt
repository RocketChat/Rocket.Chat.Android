package chat.rocket.android.server.domain

import chat.rocket.android.widget.roomupdate.UpdateObserver
import chat.rocket.core.model.ChatRoom
import javax.inject.Inject

class ObserveChatRoomsInteractor @Inject constructor(private val repository: ChatRoomsRepository) {

    fun registerObserver(observer: UpdateObserver) = repository.registerObserver(observer)

    fun removeObserver(observer: UpdateObserver) = repository.removeObserver(observer)

    fun notifyObservers(rooms: List<ChatRoom>) = repository.notifyObservers(rooms)

}