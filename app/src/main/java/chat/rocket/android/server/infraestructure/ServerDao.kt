package chat.rocket.android.server.infraestructure

import android.arch.persistence.room.*
import io.reactivex.Single

@Dao
interface ServerDao {

    @Query("SELECT * FROM server")
    fun getServers(): Single<List<ServerEntity>>

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertServer(serverEntity: ServerEntity)

    @Update
    fun updateServer(serverEntity: ServerEntity)

    @Delete
    fun deleteServer(serverEntity: ServerEntity)

    @Query("SELECT * FROM server WHERE id = :serverId")
    fun getServer(serverId: Long?): Single<ServerEntity>
}
