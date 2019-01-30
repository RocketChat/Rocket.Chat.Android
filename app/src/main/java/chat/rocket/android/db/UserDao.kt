package chat.rocket.android.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import chat.rocket.android.db.model.BaseUserEntity
import chat.rocket.android.db.model.UserEntity
import chat.rocket.android.db.model.UserStatus
import timber.log.Timber

@Dao
abstract class UserDao : BaseDao<UserEntity> {

    @Query("""
        UPDATE users set STATUS = "offline"
        """)
    abstract fun clearStatus()

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun update(user: UserEntity): Int

    @Query("UPDATE OR IGNORE users set STATUS = :status where ID = :id")
    abstract fun update(id: String, status: String): Int

    @Query("SELECT id FROM users WHERE ID = :id")
    abstract fun findUser(id: String): String?

    @Query("SELECT * FROM users WHERE ID = :id")
    abstract fun getUser(id: String): UserEntity?

    @Transaction
    open fun upsert(user: BaseUserEntity) {
        internalUpsert(user)
    }

    @Transaction
    open fun upsert(users: List<BaseUserEntity>) {
        users.forEach { internalUpsert(it) }
    }

    private inline fun internalUpsert(user: BaseUserEntity) {
        val count = if (user is UserStatus) {
            update(user.id, user.status)
        }  else {
            update(user as UserEntity)
        }
        if (count == 0 && user is UserEntity) {
            Timber.d("missing user, inserting: ${user.id}")
            insert(user)
        }
    }
}