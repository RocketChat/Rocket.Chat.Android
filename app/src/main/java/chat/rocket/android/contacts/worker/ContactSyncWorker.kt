package chat.rocket.android.contacts.worker

import android.content.Context
import android.provider.ContactsContract
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import chat.rocket.android.app.RocketChatApplication
import chat.rocket.android.contacts.models.Contact
import chat.rocket.android.dagger.injector.AndroidWorkerInjection
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.draw.dagger.DaggerAppComponent
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import dagger.Module
import dagger.android.AndroidInjection
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

class ContactSyncWorker(context : Context, params : WorkerParameters)
    : Worker(context, params) {
    @Inject
    lateinit var dbFactory: DatabaseManagerFactory
    @Inject
    lateinit var serverInteractor: GetCurrentServerInteractor
    @Inject
    lateinit var getAccountsInteractor: GetAccountsInteractor

    private var contactArrayList: ArrayList<Contact> = ArrayList()

    override fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        getContactList()

        val dbManager = dbFactory.create(serverInteractor.get()!!)
        dbManager.processContacts(contactArrayList)

        Timber.d("Contacts fetched in background.")


        return Result.SUCCESS
    }



    private fun getContactList() {
        val cr = applicationContext.contentResolver

        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME))

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    // Has phone numbers

                    val pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf<String>(id), null)
                    while (pCur!!.moveToNext()) {
                        val phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val contact = Contact()
                        contact.setName(name)
                        contact.setPhoneNumber(phoneNo)
                        contactArrayList.add(contact)
                    }
                    pCur.close()
                }

                if (true) {
                    // No check for having email address

                    val eCur = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            arrayOf<String>(id), null)
                    while (eCur!!.moveToNext()) {
                        val emailID = eCur.getString(eCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Email.DATA))
                        val contact = Contact()
                        contact.setName(name)
                        contact.setEmailAddress(emailID)
                        contactArrayList.add(contact)
                    }
                    eCur.close()
                }
            }
        }
        cur?.close()
        contactArrayList.sortWith(Comparator { o1, o2 ->
            o1.getName()!!.compareTo(o2.getName()!!)
        })
    }
}