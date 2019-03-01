package chat.rocket.android.contacts.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.contacts.models.Contact
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.util.extension.onQueryTextListener
import kotlinx.android.synthetic.main.app_bar.*
import java.util.ArrayList
import kotlin.collections.HashMap

// WIDECHAT
import chat.rocket.android.helper.Constants
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.View.GONE
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.chatrooms.adapter.ItemHolder
import chat.rocket.android.contacts.adapter.ContactsHeaderItemHolder
import chat.rocket.android.contacts.adapter.ContactsItemHolder
import chat.rocket.android.contacts.adapter.ContactsRecyclerViewAdapter
import chat.rocket.android.contacts.adapter.InviteItemHolder
import chat.rocket.android.contacts.models.ContactsLoadingState
import chat.rocket.android.contacts.presentation.ContactsPresenter
import chat.rocket.android.contacts.presentation.ContactsView
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.app_bar.view.*
import kotlinx.android.synthetic.main.fragment_contact_parent.*
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject
import timber.log.Timber

/**
 * Load a list of contacts in a recycler view
 */
class ContactsFragment : Fragment(), ContactsView {

    @Inject
    lateinit var presenter: ContactsPresenter

    @Inject
    lateinit var dbFactory: DatabaseManagerFactory

    @Inject
    lateinit var serverInteractor: GetCurrentServerInteractor

    private var recyclerView :RecyclerView? = null
    private var emptyTextView:  TextView? = null

    /**
     * The list of contacts to load in the recycler view
     */
    private var contactArrayList: ArrayList<Contact> = ArrayList()

    private val MY_PERMISSIONS_REQUEST_RW_CONTACTS = 0

    private var searchView: SearchView? = null
    private var searchIcon: ImageView? = null
    private var searchText: TextView? = null
    private var searchCloseButton: ImageView? = null
    private var loadedOnce: Boolean = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loadedOnce = false
        setHasOptionsMenu(true)
    }

    override fun onPause() {
        activity?.invalidateOptionsMenu()
        hideSpinner()
        super.onPause()
    }

    override fun onResume() {
        hideSpinner()
        loadedOnce = false
        super.onResume()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_contact_parent)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.recyclerView = view.findViewById(R.id.recycler_view)
        this.emptyTextView = view.findViewById(R.id.text_no_contacts_to_display)
        getContactsPermissions()
        setupToolbar()
    }

    private fun getContactList() {
        val serverUrl = serverInteractor.get()!!
        val dbManager = dbFactory.create(serverUrl)

        Single.fromCallable {
            // need to return a non-null object, since Rx 2 doesn't allow nulls
            dbManager.contactsDao().getAllSync()
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { contactEntities ->
                            contactArrayList = ArrayList(contactEntities.map { contactEntity ->
                                run{
                                    val contact = Contact()
                                    contact.setName(contactEntity.name!!)
                                    if (contactEntity.isPhone) {
                                        contact.setPhoneNumber(contactEntity.phoneNumber!!)
                                        contact.setIsPhone(true)
                                    } else {
                                        contact.setEmailAddress(contactEntity.emailAddress!!)
                                    }
                                    if(contactEntity.username != null) {
                                        contact.setUsername(contactEntity.username)
                                        contact.setUserId(contactEntity.userId)
                                    }
                                    contact.setAvatarUrl(serverUrl.avatarUrl(contact?.getUsername() ?: contact?.getName() ?: ""))
                                    contact
                                }
                            })
                            setupFrameLayout(contactArrayList)
                        },
                        onError = { error ->
                        }
                )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.widechat_contacts, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as? SearchView
        searchView?.onQueryTextListener { queryContacts(it) }

        if (Constants.WIDECHAT) {
            setupWidechatSearchView()
            return
        }

        searchView?.maxWidth = Integer.MAX_VALUE
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                hideSpinner()
            }
            R.id.action_refresh -> {
                (activity as MainActivity).syncContacts(true)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setupToolbar(){
        (activity as MainActivity).toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        (activity as MainActivity).toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        with((activity as AppCompatActivity?)?.supportActionBar) {
            this?.setDisplayShowTitleEnabled(true)
            this?.title = getString(R.string.title_contacts)
        }

        if (Constants.WIDECHAT) {
            with((activity as AppCompatActivity?)?.supportActionBar) {
                this?.setDisplayShowCustomEnabled(false)
            }
        }
    }

    private fun setupWidechatSearchView() {
        searchView?.setBackgroundResource(R.drawable.widechat_search_white_background)
        searchView?.isIconified = true

        searchIcon = searchView?.findViewById(R.id.search_mag_icon)
        searchIcon?.setImageResource(R.drawable.ic_search_gray_24px)

        searchText = searchView?.findViewById(R.id.search_src_text)
        searchText?.setTextColor(Color.GRAY)
        searchText?.setHintTextColor(Color.GRAY)

        searchCloseButton = searchView?.findViewById(R.id.search_close_btn)
        searchCloseButton?.setImageResource(R.drawable.ic_close_gray_24dp)

        searchCloseButton?.setOnClickListener { v ->
            searchView?.clearFocus()
            searchView?.setQuery("", false)
        }
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
                if (containsIgnoreCase(contact.getName()!!, query)) {
                    filteredContactArrayList.add(contact)
                }
            }
            setupFrameLayout(filteredContactArrayList)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_RW_CONTACTS -> {
                if (
                        grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission granted
                    launch {
                        getContactListWhenSynced()
                    }
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun getContactsPermissions() {
        if (
                ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
        ) {
            launch {
                getContactListWhenSynced()
            }
        } else {
            requestPermissions(
                    arrayOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS
                    ),
                    MY_PERMISSIONS_REQUEST_RW_CONTACTS
            )
        }
    }

    private fun getContactListWhenSynced() {
        // Show loading while sync in progress
        recyclerView!!.visibility = View.GONE
        emptyTextView!!.visibility = View.GONE

        val serverUrl = serverInteractor.get()!!
        val dbManager = dbFactory.create(serverUrl)
        val contactList = dbManager.contactsDao().getAllSync()

        ui {
            (activity as MainActivity).contactsLoadingState.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    is ContactsLoadingState.Loading -> {
                        if (contactList.isEmpty()) {
                            showLoading()
                        } else {
                            hideLoading()
                            if (state.fromRefreshButton) {
                                showSpinner()
                            }
                            if (!loadedOnce) {
                                getContactList()
                            }
                        }
                    }
                    is ContactsLoadingState.Loaded -> {
                        hideLoading()
                        hideSpinner()
                        // TODO: Show updated contacts without refreshing the whole view
                        if (state.fromRefreshButton) {
                            getContactList()
                            if (loadedOnce)
                                showToast("Contacts synced successfully", 1)
                        } else if (!loadedOnce) {
                            getContactList()
                        }
                    }
                    is ContactsLoadingState.Error -> {
                        hideLoading()
                        hideSpinner()
                        showGenericErrorMessage()
                        getContactList()
                    }
                }
            })
        }
    }

    fun setupFrameLayout(filteredContactArrayList: ArrayList<Contact>) {
        loadedOnce = true
        if (filteredContactArrayList.size == 0) {
            emptyTextView!!.visibility = View.VISIBLE
            recyclerView!!.visibility = View.GONE
        } else {
            emptyTextView!!.visibility = View.GONE
            recyclerView!!.visibility = View.VISIBLE

            recyclerView!!.setHasFixedSize(true)
            recyclerView!!.layoutManager = LinearLayoutManager(context)
            recyclerView!!.adapter = ContactsRecyclerViewAdapter(this.activity as MainActivity, presenter, map(filteredContactArrayList))
        }
    }

    fun map(contacts: List<Contact>): ArrayList<ItemHolder<*>> {
        val finalList = ArrayList<ItemHolder<*>>(contacts.size + 2)
        val userList = ArrayList<ItemHolder<*>>(contacts.size)
        val userContactList: ArrayList<Contact> = ArrayList()
        val unfilteredContactsList: ArrayList<Contact> = ArrayList()
        val contactsList = ArrayList<ItemHolder<*>>(contacts.size)
        contacts.forEach { contact ->
            if(contact.getUsername()!= null){
                // Users in their own list for filtering before adding to an ItemHolder
                userContactList.add(contact)
            } else {
                unfilteredContactsList.add(contact)
            }
        }
        // Filter for dupes
        userContactList.distinctBy { it.getUsername() }.forEach { contact ->
            userList.add(ContactsItemHolder(contact))
        }

        unfilteredContactsList.distinctBy { listOf(it.getPhoneNumber(), it.getEmailAddress())}.forEach { contact ->
            contactsList.add(ContactsItemHolder(contact))
        }

        finalList.addAll(userList)
        finalList.add(ContactsHeaderItemHolder(getString(R.string.Invite_contacts)))
        finalList.addAll(contactsList)
        finalList.add(InviteItemHolder("invite"))
        return finalList
    }

    private fun showSpinner() {
        try {
            ui {
                (activity as MainActivity).toolbar.toolbar_progress_bar.visibility = VISIBLE
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    private fun hideSpinner() {
        try {
            ui {
                (activity as MainActivity).toolbar.toolbar_progress_bar.visibility = GONE
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    override fun showLoading() {
        ui {
            view_loading.isVisible = true
            view_loading.show()
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.isVisible = false
            view_loading.hide()
        }
    }

    override fun showMessage(resId: Int) {
        ui {
            showToast(resId)
        }
    }

    override fun showMessage(message: String) {
        ui {
            showToast(message)
        }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

}
