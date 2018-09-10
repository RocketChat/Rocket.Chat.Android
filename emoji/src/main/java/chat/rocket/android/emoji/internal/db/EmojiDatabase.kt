package chat.rocket.android.emoji.internal.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import chat.rocket.android.emoji.Emoji
import chat.rocket.android.emoji.EmojiDao

@Database(entities = [Emoji::class], version = 1)
@TypeConverters(StringListConverter::class)
abstract class EmojiDatabase : RoomDatabase() {

    abstract fun emojiDao(): EmojiDao

    companion object : SingletonHolder<EmojiDatabase, Context>({
        Room.databaseBuilder(it.applicationContext,
            EmojiDatabase::class.java, "emoji.db")
            .fallbackToDestructiveMigration()
            .build()
    })
}

open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @kotlin.jvm.Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}
