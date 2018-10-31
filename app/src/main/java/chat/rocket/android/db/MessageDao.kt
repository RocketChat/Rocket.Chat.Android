package chat.rocket.android.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import chat.rocket.android.db.model.AttachmentActionEntity
import chat.rocket.android.db.model.AttachmentEntity
import chat.rocket.android.db.model.AttachmentFieldEntity
import chat.rocket.android.db.model.BaseMessageEntity
import chat.rocket.android.db.model.FullMessage
import chat.rocket.android.db.model.PartialMessage
import chat.rocket.android.db.model.MessageChannels
import chat.rocket.android.db.model.MessageEntity
import chat.rocket.android.db.model.MessageFavoritesRelation
import chat.rocket.android.db.model.MessageMentionsRelation
import chat.rocket.android.db.model.MessagesSync
import chat.rocket.android.db.model.ReactionEntity
import chat.rocket.android.db.model.UrlEntity
import chat.rocket.android.db.model.UserEntity
import timber.log.Timber
@Dao
abstract class  MessageDao {
    @Insert
    abstract fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(relation: MessageFavoritesRelation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(relation: MessageMentionsRelation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(relation: MessageChannels)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(attachment: AttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(field: AttachmentFieldEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(reaction: ReactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(url: UrlEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    abstract fun delete(id: String)

    @Query("DELETE FROM messages WHERE roomId = :roomId")
    abstract fun deleteByRoomId(roomId: String)

    @Transaction
    open fun insert(message: MessageEntity, entities: List<BaseMessageEntity>) {
        insertInternal(message, entities)
    }

    private fun insertInternal(message: MessageEntity, entities: List<BaseMessageEntity>) {
        Timber.d("Inserting message: ${message.id}, entities: ${entities.size}")
        delete(message.id)
        insert(message)
        entities.forEach { entity ->
            insert(entity)
        }
    }

    private fun insert(entity: BaseMessageEntity) {
        when(entity) {
            is MessageEntity -> insert(entity)
            is MessageFavoritesRelation -> insert(entity)
            is MessageMentionsRelation -> insert(entity)
            is MessageChannels -> insert(entity)
            is AttachmentEntity -> insert(entity)
            is AttachmentFieldEntity -> insert(entity)
            is ReactionEntity -> insert(entity)
            is UrlEntity -> insert(entity)
        }
    }

    @Transaction
    open fun insert(list: List<Pair<MessageEntity, List<BaseMessageEntity>>>) {
        list.forEach { (message, entities) ->
            insertInternal(message, entities)
        }
    }

    //@Query("SELECT * FROM messages WHERE id = :id")
    @Query("""
        $BASE_MESSAGE_QUERY
        WHERE messages.id = :id
        """)
    abstract fun internalGetMessageById(id: String): PartialMessage?

    @Transaction
    open fun getMessageById(id: String): FullMessage? {
        return internalGetMessageById(id)?.let { message ->
            retrieveFullMessage(message)
        }
    }

    @Query("""
        $BASE_MESSAGE_QUERY
        WHERE messages.roomId = :roomId
        ORDER BY messages.timestamp DESC
        """
    )
    abstract fun internalGetMessagesByRoomId(roomId: String): List<PartialMessage>

    @Query("""
        $BASE_MESSAGE_QUERY
        WHERE messages.roomId = :roomId
        ORDER BY messages.timestamp DESC
        LIMIT :count
        """
    )
    abstract fun internalGetRecentMessagesByRoomId(roomId: String, count: Long): List<PartialMessage>

    @Transaction
    open fun getMessagesByRoomId(roomId: String): List<FullMessage> {
        return internalGetMessagesByRoomId(roomId).map { message ->
            retrieveFullMessage(message)
        }
    }

    @Transaction
    open fun getRecentMessagesByRoomId(roomId: String, count: Long): List<FullMessage> {
        return internalGetRecentMessagesByRoomId(roomId, count).map { message ->
            retrieveFullMessage(message)
        }
    }

    @Query("""
        SELECT * FROM users WHERE users.id IN
            (SELECT userId FROM message_favorites WHERE messageId = :messageId)
        """)
    abstract fun getFavoritesByMessage(messageId: String): List<UserEntity>

    @Query("""
        SELECT * FROM users WHERE users.id IN
            (SELECT userId FROM message_mentions WHERE messageId = :messageId)
        """)
    abstract fun getMentionsByMessage(messageId: String): List<UserEntity>

    @Query("""
            $BASE_MESSAGE_QUERY
            WHERE synced = 0
            ORDER BY messages.timestamp DESC
        """)
    abstract fun internalUnsetMessages(): List<PartialMessage>

    @Transaction
    open fun getUnsentMessages(): List<FullMessage> {
        return internalUnsetMessages().map { message ->
            retrieveFullMessage(message)
        }
    }

    internal fun retrieveFullMessage(message: PartialMessage): FullMessage {
        val favorites = getFavoritesByMessage(message.message.id)
        val mentions = getMentionsByMessage(message.message.id)
        return FullMessage(message, favorites, mentions)
    }

    @Query("SELECT * FROM messages_sync WHERE roomId = :roomId")
    abstract fun getLastSync(roomId: String): MessagesSync?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun saveLastSync(entity: MessagesSync)

    @Query("SELECT * FROM attachment_fields WHERE attachmentId = :id")
    abstract fun getAttachmentFields(id: String): List<AttachmentFieldEntity>

    @Query("SELECT * FROM attachment_action WHERE attachmentId = :id")
    abstract fun getAttachmentActions(id: String): List<AttachmentActionEntity>

    companion object {
        const val BASE_MESSAGE_QUERY = """
            SELECT
                messages.*,
			    senderBy.name as senderName,
			    senderBy.username as senderUsername,
    		    editBy.name as editName,
			    editBy.username as editUsername
            FROM messages
            LEFT JOIN urls as u ON u.messageId = messages.id
            LEFT JOIN attachments as attachment ON attachment.message_id = messages.id
            LEFT JOIN reactions ON reactions.messageId = messages.id
            LEFT JOIN message_channels ON message_channels.messageId = messages.id
			LEFT JOIN users as senderBy ON messages.senderId = senderBy.id
			LEFT JOIN users as editBy ON messages.editedBy = editBy.id
        """
    }
}