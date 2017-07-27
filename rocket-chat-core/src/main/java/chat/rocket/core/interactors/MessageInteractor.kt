package chat.rocket.core.interactors

import com.hadisatrio.optional.Optional
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.UUID
import chat.rocket.core.SyncState
import chat.rocket.core.models.Message
import chat.rocket.core.models.Room
import chat.rocket.core.models.RoomHistoryState
import chat.rocket.core.models.User
import chat.rocket.core.repositories.MessageRepository
import chat.rocket.core.repositories.RoomRepository

class MessageInteractor(private val messageRepository: MessageRepository,
                        private val roomRepository: RoomRepository) {

    fun loadMessages(room: Room): Single<Boolean> {
        val roomHistoryState = RoomHistoryState.builder()
                .setRoomId(room.roomId)
                .setSyncState(SyncState.NOT_SYNCED)
                .setCount(100)
                .setReset(true)
                .setComplete(false)
                .setTimestamp(0)
                .build()

        return roomRepository.setHistoryState(roomHistoryState)
    }

    fun loadMoreMessages(room: Room): Single<Boolean> {
        return roomRepository.getHistoryStateByRoomId(room.roomId)
                .filter { it.isPresent }
                .map { it.get() }
                .filter { roomHistoryState ->
                    val syncState = roomHistoryState.syncState
                    !roomHistoryState.isComplete && (syncState == SyncState.SYNCED || syncState == SyncState.FAILED)
                }
                .map { Optional.of(it) }
                .first(Optional.absent())
                .flatMap { historyStateOptional ->
                    if (!historyStateOptional.isPresent) {
                        return@flatMap Single.just(false)
                    }
                    roomRepository
                            .setHistoryState(historyStateOptional.get().withSyncState(SyncState.NOT_SYNCED))
                }
    }

    fun send(destination: Room, sender: User, messageText: String): Single<Boolean> {
        val message = Message.builder()
                .setId(UUID.randomUUID().toString())
                .setSyncState(SyncState.NOT_SYNCED)
                .setTimestamp(System.currentTimeMillis())
                .setRoomId(destination.roomId)
                .setMessage(messageText)
                .setGroupable(false)
                .setUser(sender)
                .setEditedAt(0)
                .build()

        return messageRepository.save(message)
    }

    fun resend(message: Message, sender: User): Single<Boolean> {
        return messageRepository.save(
                message.withSyncState(SyncState.NOT_SYNCED).withUser(sender))
    }

    fun update(message: Message, sender: User, content: String): Single<Boolean> {
        return messageRepository.save(
                message.withSyncState(SyncState.NOT_SYNCED)
                        .withUser(sender)
                        .withMessage(content)
                        .withEditedAt(message.editedAt + 1))
    }

    fun delete(message: Message): Single<Boolean> {
        return messageRepository.delete(message)
    }

    fun unreadCountFor(room: Room, user: User): Single<Int> {
        return messageRepository.unreadCountFor(room, user)
    }

    fun getAllFrom(room: Room): Flowable<List<Message>> {
        return messageRepository.getAllFrom(room)
    }
}
