package chat.rocket.android.emoji

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import androidx.room.Update

@Dao
interface EmojiDao {

    @Query("SELECT * FROM emoji")
    fun loadAllEmojis(): List<Emoji>

    @Query("SELECT * FROM emoji WHERE url IS NULL")
    fun loadSimpleEmojis(): List<Emoji>

    @Query("SELECT * FROM emoji WHERE url IS NOT NULL")
    fun loadAllCustomEmojis(): List<Emoji>

    @Query("SELECT * FROM emoji WHERE shortname=:shortname")
    fun loadEmojiByShortname(shortname: String): List<Emoji>

    @Query("SELECT * FROM emoji WHERE UPPER(category)=UPPER(:category)")
    fun loadEmojisByCategory(category: String): List<Emoji>

    @Query("SELECT * FROM emoji WHERE UPPER(category)=UPPER(:category) AND url LIKE :url")
    fun loadEmojisByCategoryAndUrl(category: String, url: String): List<Emoji>

    @Insert(onConflict = IGNORE)
    fun insertEmoji(emoji: Emoji)

    @Insert(onConflict = IGNORE)
    fun insertAllEmojis(vararg emojis: Emoji)

    @Update
    fun updateEmoji(emoji: Emoji)

    @Delete
    fun deleteEmoji(emoji: Emoji)

    @Query("DELETE FROM emoji")
    fun deleteAll()
}
