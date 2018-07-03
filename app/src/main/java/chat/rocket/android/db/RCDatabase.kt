package chat.rocket.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import chat.rocket.android.db.model.AttachmentEntity
import chat.rocket.android.db.model.AttachmentFieldEntity
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.db.model.MessageChannelsRelation
import chat.rocket.android.db.model.MessageEntity
import chat.rocket.android.db.model.MessageFavoritesRelation
import chat.rocket.android.db.model.MessageMentionsRelation
import chat.rocket.android.db.model.ReactionEntity
import chat.rocket.android.db.model.ReactionMessageRelation
import chat.rocket.android.db.model.UrlEntity
import chat.rocket.android.db.model.UserEntity

@Database(
    entities = [
        UserEntity::class, ChatRoomEntity::class, MessageEntity::class,
        MessageFavoritesRelation::class, MessageMentionsRelation::class,
        MessageChannelsRelation::class, AttachmentEntity::class,
        AttachmentFieldEntity::class, UrlEntity::class, ReactionEntity::class,
        ReactionMessageRelation::class
    ],
    version = 6,
    exportSchema = true
)
abstract class RCDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun messageDao(): MessageDao
}