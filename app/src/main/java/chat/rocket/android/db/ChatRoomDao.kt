package chat.rocket.android.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import chat.rocket.android.db.model.ChatRoom
import chat.rocket.android.db.model.ChatRoomEntity

@Dao
abstract class ChatRoomDao : BaseDao<ChatRoomEntity> {
    @Transaction
    @Query("""
        $BASE_QUERY
        WHERE chatrooms.id = :id
        """)
    abstract fun get(id: String): LiveData<ChatRoom>

    @Transaction
    @Query("""
        $BASE_QUERY
        WHERE chatrooms.id = :id
        """)
    abstract fun getSync(id: String): ChatRoom?

    @Transaction
    @Query("$BASE_QUERY $FILTER_NOT_OPENED")
    abstract fun getAllSync(): List<ChatRoom>

    @Transaction
    @Query("""$BASE_QUERY
            WHERE chatrooms.name LIKE '%' || :query || '%'
            OR  users.name LIKE '%' || :query || '%'
            """)
    abstract fun searchSync(query: String): List<ChatRoom>

    @Query("SELECT COUNT(id) FROM chatrooms WHERE open = 1")
    abstract fun count(): Long

    @Transaction
    @Query("""
        $BASE_QUERY
        $FILTER_NOT_OPENED
        ORDER BY
	        CASE
		        WHEN lastMessageTimeStamp IS NOT NULL THEN lastMessageTimeStamp
		        ELSE updatedAt
	        END DESC
        """)
    abstract fun getAll(): LiveData<List<ChatRoom>>

    @Transaction
    @Query("""
        $BASE_QUERY
        $FILTER_NOT_OPENED
        ORDER BY
            $TYPE_ORDER,
	        CASE
		        WHEN lastMessageTimeStamp IS NOT NULL THEN lastMessageTimeStamp
		        ELSE updatedAt
	        END DESC
        """)
    abstract fun getAllGrouped(): LiveData<List<ChatRoom>>

    @Transaction
    @Query("""
        $BASE_QUERY
        $FILTER_NOT_OPENED
        ORDER BY name
        """)
    abstract fun getAllAlphabetically(): LiveData<List<ChatRoom>>

    @Transaction
    @Query("""
        $BASE_QUERY
        $FILTER_NOT_OPENED
        ORDER BY
            $TYPE_ORDER,
            name
        """)
    abstract fun getAllAlphabeticallyGrouped(): LiveData<List<ChatRoom>>

    @Query("DELETE FROM chatrooms WHERE ID = :id")
    abstract fun delete(id: String)

    @Query("DELETE FROM chatrooms")
    abstract fun delete()

    @Transaction
    open fun cleanInsert(chatRooms: List<ChatRoomEntity>) {
        delete()
        insert(chatRooms)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplace(chatRooms: List<ChatRoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplace(chatRoom: ChatRoomEntity)

    @Update
    abstract fun update(list: List<ChatRoomEntity>)

    @Transaction
    open fun update(toInsert: List<ChatRoomEntity>, toUpdate: List<ChatRoomEntity>, toRemove: List<String>) {
        insertOrReplace(toInsert)
        update(toUpdate)
        toRemove.forEach { id ->
            delete(id)
        }
    }

    companion object {
        const val BASE_QUERY = """
            SELECT chatrooms.*,
                users.username as username,
                users.name as userFullname,
                users.status,
                lmUsers.username as lastMessageUserName,
                lmUsers.name as lastMessageUserFullName
            FROM chatrooms
            LEFT JOIN users ON chatrooms.userId = users.id
            LEFT JOIN users AS lmUsers ON chatrooms.lastMessageUserId = lmUsers.id
        """

        const val FILTER_NOT_OPENED = """
            WHERE chatrooms.open = 1
        """

        const val TYPE_ORDER = """
            CASE
		        WHEN type = 'c' THEN 1
		        WHEN type = 'p' THEN 2
		        WHEN type = 'd' THEN 3
		        WHEN type = 'l' THEN 4
		        ELSE 5
	        END
        """
    }
}