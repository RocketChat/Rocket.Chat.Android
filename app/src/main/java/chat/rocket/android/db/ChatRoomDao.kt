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
import chat.rocket.common.model.RoomType

@Dao
abstract class ChatRoomDao : BaseDao<ChatRoomEntity> {
    @Transaction
    @Query("""
        $BASE_QUERY
        WHERE chatrooms.id = :id
        """)
    abstract fun get(id: String): ChatRoom?

    @Transaction
    @Query("""
        $BASE_QUERY
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
        ORDER BY name
        """)
    abstract fun getAllAlphabetically(): LiveData<List<ChatRoom>>

    @Transaction
    @Query("""
        $BASE_QUERY
        ORDER BY
            $TYPE_ORDER,
            name
        """)
    abstract fun getAllAlphabeticallyGrouped(): LiveData<List<ChatRoom>>

    @Query("DELETE FROM chatrooms WHERE ID = :id")
    abstract fun delete(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplace(chatRooms: List<ChatRoomEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrReplace(chatRoom: ChatRoomEntity)

    @Update
    abstract fun update(list: List<ChatRoomEntity>)

    @Transaction
    open fun update(toRemove: List<String>, toInsert: List<ChatRoomEntity>, toUpdate: List<ChatRoomEntity>) {
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

        const val TYPE_ORDER = """
            CASE
		        WHEN type = '${RoomType.CHANNEL}' THEN 1
		        WHEN type = '${RoomType.PRIVATE_GROUP}' THEN 2
		        WHEN type = '${RoomType.DIRECT_MESSAGE}' THEN 3
		        WHEN type = '${RoomType.LIVECHAT}' THEN 4
		        ELSE 5
	        END
        """
    }
}