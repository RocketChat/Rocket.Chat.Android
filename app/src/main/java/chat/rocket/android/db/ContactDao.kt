package chat.rocket.android.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import chat.rocket.android.contacts.models.Contact
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.db.model.ContactEntity

@Dao
abstract class ContactDao : BaseDao<ContactEntity> {
    @Transaction
    @Query("""
        SELECT * FROM contacts
    """)
    abstract fun getAllSync(): List<Contact>

    @Query("DELETE FROM contacts")
    abstract fun delete()

    @Transaction
    open fun cleanInsert(contacts: List<ContactEntity>) {
        delete()
        insert(contacts)
    }
}