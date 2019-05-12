package chat.rocket.android.contacts.worker

import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.work.Worker
import androidx.work.WorkerParameters
import chat.rocket.android.contacts.models.Contact
import chat.rocket.android.dagger.injector.AndroidWorkerInjection
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.common.RocketChatException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.internal.rest.queryContacts
import chat.rocket.core.model.ContactHolder
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.security.MessageDigest
import java.util.ArrayList
import javax.inject.Inject

class ContactsSyncWorker(context : Context, params : WorkerParameters)
    : Worker(context, params) {
    @Inject
    lateinit var dbFactory: DatabaseManagerFactory
    @Inject
    lateinit var serverInteractor: GetCurrentServerInteractor
    @Inject
    lateinit var getAccountsInteractor: GetAccountsInteractor
    @Inject
    lateinit var factory: RocketChatClientFactory


    private var contactArrayList: ArrayList<Contact> = ArrayList()

    override fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        val currentServer = serverInteractor.get()
        if (currentServer == null){
            return Result.RETRY
        }
        val client: RocketChatClient = factory.create(currentServer)
        if (!(client.state is State.Connected)){
            return Result.RETRY
        }

        getContactList()
        val dbManager = dbFactory.create(serverInteractor.get()!!)

        val strongHashes: List<String> = (contactArrayList.map { contact -> hashString(contact.getDetail()!!) })
        val weakHashes: List<String> = strongHashes.map{ strongHash -> strongHash.substring(3,9) }
        var retryFlag = false
        runBlocking {
            try {
                val apiResult: List<ContactHolder>? = client.queryContacts(weakHashes)
                if (apiResult != null) {
                    val intersectionMap: HashMap<String, List<String>> = HashMap()
                    val intersection: List<String> = apiResult.mapIndexed { index, list ->
                        run {
                            intersectionMap.put(list.h, listOf(list.id, list.u))
                            list.h
                        }
                    }
                    val intersectionSet: Set<String> = intersection.toSet()
                    contactArrayList.forEachIndexed { index, contact ->
                        run {
                            if (strongHashes[index] in intersectionSet) {
                                contact.setUserId(intersectionMap[strongHashes[index]]?.first())
                                contact.setUsername(intersectionMap[strongHashes[index]]?.last())
                            }
                        }
                    }
                }
                Timber.d("Contacts fetched in background.")
            } catch (ex: RocketChatException){
                retryFlag = true
            } finally {
                dbManager.processContacts(contactArrayList)
            }
        }
        if (retryFlag) {
            return Result.RETRY
        }
        return Result.SUCCESS
    }

    private fun getContactList() {
        var country = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            country = applicationContext.getResources().getConfiguration().locales.get(0).country
        } else{
            country = applicationContext.getResources().getConfiguration().locale.getCountry();
        }

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
                        val formattedPhone = formatPhoneNumber(phoneNo, country)
                        if(formattedPhone!=null) {
                            val contact = Contact()
                            contact.setName(name)
                            contact.setPhoneNumber(formattedPhone)
                            contactArrayList.add(contact)
                        }
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
                        contact.setEmailAddress(emailID.toLowerCase())
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

    private fun hashString(input: String): String {
        val HEX_CHARS = "0123456789abcdef"
        val bytes = MessageDigest
                .getInstance("SHA-1")
                .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }

    private fun formatPhoneNumber(phone: String, country:String): String? {
        return PhoneNumberUtils.formatNumberToE164(phone.replace("-|\\s|\\(|\\)".toRegex(), ""), country)
    }

}
