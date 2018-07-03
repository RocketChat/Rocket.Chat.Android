package chat.rocket.android.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import chat.rocket.android.db.model.AttachmentEntity
import chat.rocket.android.db.model.AttachmentFieldEntity
import chat.rocket.android.db.model.BaseMessageEntity
import chat.rocket.android.db.model.MessageChannelsRelation
import chat.rocket.android.db.model.MessageEntity
import chat.rocket.android.db.model.MessageFavoritesRelation
import chat.rocket.android.db.model.MessageMentionsRelation
import chat.rocket.android.db.model.ReactionEntity
import chat.rocket.android.db.model.ReactionMessageRelation
import chat.rocket.android.db.model.UrlEntity
import timber.log.Timber

@Dao
abstract class  MessageDao {
    @Insert
    abstract fun insert(message: MessageEntity)

    @Insert
    abstract fun insert(relation: MessageFavoritesRelation)

    @Insert
    abstract fun insert(relation: MessageMentionsRelation)

    @Insert
    abstract fun insert(relation: MessageChannelsRelation)

    @Insert
    abstract fun insert(attachment: AttachmentEntity)

    @Insert
    abstract fun insert(field: AttachmentFieldEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(reaction: ReactionEntity)

    @Insert
    abstract fun insert(relation: ReactionMessageRelation)

    @Insert
    abstract fun insert(url: UrlEntity)

    @Query("DELETE FROM messages WHERE id = :id")
    abstract fun delete(id: String)

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
            is MessageChannelsRelation -> insert(entity)
            is AttachmentEntity -> insert(entity)
            is AttachmentFieldEntity -> insert(entity)
            is ReactionEntity -> insert(entity)
            is ReactionMessageRelation -> insert(entity)
            is UrlEntity -> insert(entity)
        }
    }

    @Transaction
    open fun insert(list: List<Pair<MessageEntity, List<BaseMessageEntity>>>) {
        list.forEach { (message, entities) ->
            insertInternal(message, entities)
        }
    }
}