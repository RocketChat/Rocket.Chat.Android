package chat.rocket.android.server.infraestructure;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface ServerDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insertServer(ServerEntity serverEntity);

    @Update
    void updateServer(ServerEntity serverEntity);

    @Delete
    void deleteServer(ServerEntity serverEntity);

    @Query("SELECT * FROM server")
    Single<List<ServerEntity>> getServers();

    @Query("SELECT * FROM server WHERE id = :serverId")
    Single<ServerEntity> getServer(Long serverId);
}
