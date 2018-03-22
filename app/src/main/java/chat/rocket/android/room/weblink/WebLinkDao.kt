package chat.rocket.android.room.weblink

import android.arch.persistence.room.*

@Dao
interface WebLinkDao {

    @Query("SELECT * FROM weblink")
    fun getWebLinks(): List<WebLinkEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertWebLink(webLinkEntity: WebLinkEntity)

    @Update
    fun updateWebLink(webLinkEntity: WebLinkEntity)

    @Delete
    fun deleteWebLink(webLinkEntity: WebLinkEntity)

    @Query("SELECT * FROM weblink WHERE link = :webLink")
    fun getWebLink(webLink: String?): WebLinkEntity
}