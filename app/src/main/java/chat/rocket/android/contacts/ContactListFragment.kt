package chat.rocket.android.contacts

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.contacts.models.Contact
import timber.log.Timber
import java.util.ArrayList
import kotlin.collections.HashMap


/**
 * Load a list of contacts in a recycler view
 */
class ContactListFragment : Fragment() {
    /**
     * The list of contacts to load in the recycler view
     */
    private var contactArrayList: ArrayList<Contact> = ArrayList()

    /**
     *  The mapping of contacts with their registration status
     */
    private var contactHashMap: HashMap<String, String> = HashMap()

    private val MY_PERMISSIONS_REQUEST_RW_CONTACTS = 0

    private fun getContactList() {
        val cr = context!!.contentResolver
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME))

                if (cur.getInt(cur.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf<String>(id), null)
                    while (pCur!!.moveToNext()) {
                        val phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val contact: Contact = Contact()
                        contact.setName(name)
                        contact.setPhoneNumber(phoneNo)
                        contactArrayList.add(contact)
                        contactHashMap[phoneNo] = "INDETERMINATE"
                    }
                    pCur.close()
                    contactArrayList.sortWith(Comparator { o1, o2 -> o1.getName()!!.compareTo(o2.getName()!!)
                    })
                }
            }
        }
        cur?.close()
    }


    private fun setupToolbar() {
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.title_contacts)
    }

    private fun populateContacts(actualContacts: Boolean) {
        if (actualContacts) {
            getContactList()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_RW_CONTACTS -> {
                if (
                        grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission granted
                    populateContacts(true)
                } else {
                    populateContacts(false)
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (
                ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
        ) {
            populateContacts(true)
        } else {
            ActivityCompat.requestPermissions(
                    this.activity as Activity,
                    arrayOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS
                    ),
                    MY_PERMISSIONS_REQUEST_RW_CONTACTS
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        val context = view.context

        val recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
        val emptyTextView = view.findViewById(R.id.text_no_data_to_display) as TextView

        if (contactArrayList.size == 0) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = ContactRecyclerViewAdapter(context, contactArrayList, contactHashMap)
        }
        setupToolbar()

        return view
    }

    companion object {

        /**
         * Create a new ContactList fragment that displays the given list of contacts
         *
         * @param contactArrayList the list of contacts to load in the recycler view
         * @param contactHashMap the mapping of contacts with their registration status
         * @return the newly created ContactList fragment
         */
        fun newInstance(
                contactArrayList: ArrayList<Contact>,
                contactHashMap: HashMap<String, String>
        ): ContactListFragment {
            val contactListFragment = ContactListFragment()

            val arguments = Bundle()
            arguments.putParcelableArrayList("CONTACT_ARRAY_LIST", contactArrayList)
            arguments.putSerializable("CONTACT_HASH_MAP", contactHashMap)

            contactListFragment.arguments = arguments

            return contactListFragment
        }
    }
}
