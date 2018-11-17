package chat.rocket.android.contacts

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.contacts.models.Contact
import chat.rocket.android.createchannel.ui.CreateChannelFragment
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.util.extension.onQueryTextListener
import kotlinx.android.synthetic.main.app_bar.*
import java.util.ArrayList
import kotlin.Comparator
import kotlin.collections.HashMap

// WIDECHAT
import chat.rocket.android.helper.Constants
import com.facebook.drawee.view.SimpleDraweeView

/**
 * Load a list of contacts in a recycler view
 */
class ContactsFragment : Fragment() {
    /**
     * The list of contacts to load in the recycler view
     */
    private var contactArrayList: ArrayList<Contact> = ArrayList()

    /**
     *  The mapping of contacts with their registration status
     */
    private var contactHashMap: HashMap<String, String> = HashMap()

    private val MY_PERMISSIONS_REQUEST_RW_CONTACTS = 0

    private var createNewChannelLink: View? = null
    private var searchView: SearchView? = null
    private var sortView: MenuItem? = null

    // WIDECHAT
    private var profileButton: SimpleDraweeView? = null
    private var widechatSearchView: SearchView? = null

    private fun getContactList() {
        val cr = context!!.contentResolver

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
                        contactHashMap[phoneNo] = "INDETERMINATE"
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
                        contactHashMap[emailID] = "INDETERMINATE"
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chatrooms, menu)

        sortView = menu.findItem(R.id.action_sort)
        sortView!!.isVisible = false

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as? SearchView
        searchView?.setIconifiedByDefault(false)
        searchView?.maxWidth = Integer.MAX_VALUE
        searchView?.onQueryTextListener { queryContacts(it) }

        if (Constants.WIDECHAT) {
            searchItem?.isVisible = false
        }

        val expandListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Simply setting sortView to visible won't work, so we invalidate the options
                // to recreate the entire menu...
                activity?.invalidateOptionsMenu()
                queryContacts("")
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }
        }
        searchItem?.setOnActionExpandListener(expandListener)
    }

    fun containsIgnoreCase(src: String, what: String): Boolean {
        val length = what.length
        if (length == 0)
            return true // Empty string is contained

        val firstLo = Character.toLowerCase(what[0])
        val firstUp = Character.toUpperCase(what[0])

        for (i in src.length - length downTo 0) {
            // Quick check before calling the more expensive regionMatches() method:
            val ch = src[i]
            if (ch != firstLo && ch != firstUp)
                continue

            if (src.regionMatches(i, what, 0, length, ignoreCase = true))
                return true
        }

        return false
    }

    fun queryContacts(query: String) {
        if (query.isBlank() or query.isEmpty()) {
            setupFrameLayout(contactArrayList)
        } else {
            var filteredContactArrayList: ArrayList<Contact> = ArrayList()
            for (contact in contactArrayList) {
                if (containsIgnoreCase(contact.getName()!!, query)
                        || (contact.isPhone() && containsIgnoreCase(contact.getPhoneNumber()!!, query))
                        || (!contact.isPhone() && containsIgnoreCase(contact.getEmailAddress()!!, query))
                ) {
                    filteredContactArrayList.add(contact)
                }
            }
            setupFrameLayout(filteredContactArrayList)
        }
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
        setHasOptionsMenu(true)

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

        // Filter before sending to FrameLayout
        setupFrameLayout(contactArrayList)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    fun setupToolbar(){
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        (activity as MainActivity).toolbar.setNavigationOnClickListener { activity?.onBackPressed()}

        if (Constants.WIDECHAT) {
            with((activity as AppCompatActivity?)?.supportActionBar) {
                profileButton = this?.getCustomView()?.findViewById(R.id.profile_image_avatar)
                profileButton?.visibility = View.GONE
                widechatSearchView = this?.getCustomView()?.findViewById(R.id.action_widechat_search)
                widechatSearchView?.visibility = View.VISIBLE
                widechatSearchView?.onQueryTextListener { queryContacts(it) }
            }
        } else {
            (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_contacts)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
    }

    fun setupFrameLayout(filteredContactArrayList: ArrayList<Contact>) {
        try {
            val contactListFragment = ContactListFragment.newInstance(
                    filteredContactArrayList,
                    contactHashMap
            )
            val fragmentTransaction = childFragmentManager.beginTransaction()
            fragmentTransaction.replace(
                    R.id.contacts_area,
                    contactListFragment,
                    "CONTACT_LIST_FRAGMENT"
            )
            fragmentTransaction.commit()
        } catch (exception: IllegalStateException) {
            //This is one bad user who clicks too fast
        } catch (exception: NullPointerException) {
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_contact_parent, container, false)

        createNewChannelLink = view.findViewById(R.id.create_new_channel_button)
        createNewChannelLink!!.setOnClickListener {
            val createChannelFragment = CreateChannelFragment()
            val transaction = activity?.supportFragmentManager?.beginTransaction();
            transaction?.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            transaction?.replace(this.id, createChannelFragment, "createChannelFragment");
            transaction?.addToBackStack(null)?.commit();
        }

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
        ): ContactsFragment {
            val contactsFragment = ContactsFragment()

            val arguments = Bundle()
            arguments.putParcelableArrayList("CONTACT_ARRAY_LIST", contactArrayList)
            arguments.putSerializable("CONTACT_HASH_MAP", contactHashMap)

            contactsFragment.arguments = arguments

            return contactsFragment
        }
    }

}
