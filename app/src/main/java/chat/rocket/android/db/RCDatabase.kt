package chat.rocket.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.db.model.UserEntity

@Database(
    entities = [UserEntity::class, ChatRoomEntity::class],
    version = 3,
    exportSchema = true
)
abstract class RCDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun chatRoomDao(): ChatRoomDao
}