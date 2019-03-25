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
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.chatrooms.adapter.ItemHolder
import chat.rocket.android.contacts.adapter.*
import chat.rocket.android.chatrooms.adapter.RoomUiModelMapper
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.contacts.models.ContactsLoadingState
import chat.rocket.android.contacts.presentation.ContactsPresenter
import chat.rocket.android.contacts.presentation.ContactsView
import chat.rocket.android.createchannel.ui.CreateChannelFragment
import chat.rocket.android.db.DatabaseManagerFactory
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

    //FIXME: Fix the blank selected contacts recycler view upon screen rotate
    //TODO: Add animations to ticks that appear upon selecting a contact
    //TODO: Add animation to contacts list translating down when contacts are selected
    //FIXME: Remove the blink when a contact is selected again after all the contacts are deselected
    //TODO: When the group is being selected via long press from the new chat screen, change the title to create group from new chat
    //TODO: Add support for square avatars during group creation flow
    //FIXME: Invite button not working during group creation

    @Inject
    lateinit var presenter: ContactsPresenter

    @Inject
    lateinit var dbFactory: DatabaseManagerFactory

    @Inject
    lateinit var serverInteractor: GetCurrentServerInteractor

    @Inject
    lateinit var mapper: RoomUiModelMapper

    private lateinit var contactsAdapter: ContactsRecyclerViewAdapter
    private lateinit var recyclerView: RecyclerView
    private var emptyTextView: TextView? = null
    private var fab: View? = null

    private lateinit var selectedContactsRecyclerView: RecyclerView
    private lateinit var selectedContactsAdapter: RecyclerView.Adapter<*>

    /**
     * The list of contacts to load in the recycler view
     */
    private var contactArrayList: ArrayList<Contact> = ArrayList()
    private var finalList: ArrayList<ItemHolder<*>> = ArrayList()
    private var contactsSelectionTracker: SelectionTracker<Long>? = null
    private var selectedContacts: ArrayList<Contact> = ArrayList()

    private var searchView: SearchView? = null
    private var searchIcon: ImageView? = null
    private var searchText: TextView? = null
    private var searchCloseButton: ImageView? = null
    private var loadedOnce: Boolean = false
    var enableGroups: Boolean = false

    companion object {
        /**
         * Create a new ContactList fragment that displays the given list of contacts
         *
         * @param contactArrayList the list of contacts to load in the recycler view
         * @param contactHashMap the mapping of contacts with their registration status
         * @return the newly created ContactList fragment
         */
        fun newInstance(
                contactArrayList: ArrayList<Contact>? = null,
                contactHashMap: HashMap<String, String>? = null,
                enableGroups: Boolean = false
        ): ContactsFragment {
            val contactsFragment = ContactsFragment()

            val arguments = Bundle()
            arguments.putParcelableArrayList("CONTACTS_ARRAY_LIST", contactArrayList)
            arguments.putSerializable("CONTACTS_HASH_MAP", contactHashMap)
            arguments.putBoolean("CONTACTS_ENABLE_GROUPS", enableGroups)

            contactsFragment.arguments = arguments
            return contactsFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loadedOnce = false
        setHasOptionsMenu(true)
        val bundle = arguments
        enableGroups = bundle?.getBoolean("CONTACTS_ENABLE_GROUPS") ?: false
    }

    override fun onPause() {
        activity?.invalidateOptionsMenu()
        hideSpinner()
        this.arguments?.putParcelableArrayList("SELECTED_CONTACTS", selectedContacts)
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

        emptyTextView = view.findViewById(R.id.text_no_contacts_to_display)
        recyclerView = view.findViewById(R.id.contacts_recycler_view)

        contactsAdapter = ContactsRecyclerViewAdapter(this, presenter, finalList)
        recyclerView = contacts_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = contactsAdapter
        }

        selectedContactsAdapter = SelectedContactsAdapter(selectedContacts, true) { contact: Contact ->
            removeFromSelectedContact(contact)
            removeFromSelectionTracker(contact)
            onSelectionChanged(selectedContacts.size > 0)
        }

        selectedContactsRecyclerView = selected_contacts_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = selectedContactsAdapter
        }

        val bundle = arguments
        bundle?.getParcelableArrayList<Contact>("SELECTED_CONTACTS")?.let {
            if (it.isNotEmpty())
                selectedContacts = it
        }

        if (hasContactsPermissions()) {
            launch {
                getContactListWhenSynced()
                ui {
                    setupTrackerAndFab()
                }
            }
        } else {
            setupFrameLayout()
        }
        setupToolbar()
    }

    private fun getContactList() {
        val serverUrl = serverInteractor.get()!!
        val dbManager = dbFactory.create(serverUrl)
        val compositeDisposable = CompositeDisposable()

        val disposable = Single.fromCallable {
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
                                    if (contactEntity.username != null) {
                                        contact.setUsername(contactEntity.username)
                                        contact.setUserId(contactEntity.userId)
                                    }
                                    contact.setAvatarUrl(serverUrl.avatarUrl(contact.getUsername() ?: contact.getName() ?: ""))
                                    contact
                                }
                            })
                            setupFrameLayout(contactArrayList)
                        },
                        onError = { error ->
                        }
                )
        compositeDisposable.add(disposable)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.widechat_contacts, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as? SearchView
        searchView?.onQueryTextListener { queryContacts(it) }

        if (Constants.WIDECHAT) {
            setupWidechatSearchView()
        }

        searchView?.maxWidth = Integer.MAX_VALUE
        val expandListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Simply setting sortView to visible won't work, so we invalidate the options
                // to recreate the entire menu...
                searchView?.setQuery("", false)
                activity?.invalidateOptionsMenu()
                queryContacts("")
                if (!hasContactsPermissions()) {
                    setupFrameLayout()
                }
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
                if(enableGroups)
                    this?.title = getString(R.string.title_create_group)
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

    fun mapSpotlightToContacts(result:List<ItemHolder<*>>): ArrayList<ItemHolder<*>> {
        val list = ArrayList<ItemHolder<*>>(result.size)
        result.forEach { item ->
            val data = item.data as RoomUiModel
            val contact = Contact()
            contact.setName(data.name.toString())
            if(data.username !==null){
                contact.setUsername(data.username)
            }
            contact.setIsSpotlightResult(true)
            contact.setAvatarUrl(data.avatar)
            contact.setUserId(data.id)
            list.add(ContactsItemHolder(contact))
        }
        return list
    }

    private fun queryContacts(query: String) {
        var result: ArrayList<ItemHolder<*>>? = null
        if (query.isBlank() or query.isEmpty()) {
            if (hasContactsPermissions()) {
                setupFrameLayout(contactArrayList)
            } else {
                result = arrayListOf()
                setupFrameLayout(result)
            }
        } else {
            val filteredContactArrayList: ArrayList<Contact> = ArrayList()
            for (contact in contactArrayList) {
                if (containsIgnoreCase(contact.getName()!!, query)) {
                    filteredContactArrayList.add(contact)
                }
            }
            launch {
                try {
                    result = presenter.spotlight(query).let { mapper.map(it, showLastMessage = false) }.let { mapSpotlightToContacts(it) }
                } catch (e: Exception) {
                    Timber.e(e)
                }
                ui {
                    if (hasContactsPermissions()) {
                        setupFrameLayout(filteredContactArrayList, result)
                    } else {
                        setupFrameLayout(result)
                    }
                }
            }
        }
    }

    private fun hasContactsPermissions() : Boolean {
        return (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }

    private fun getContactListWhenSynced() {
        // Show loading while sync in progress
        recyclerView.visibility = View.GONE
        emptyTextView!!.visibility = View.GONE

        val serverUrl = serverInteractor.get()!!
        val dbManager = dbFactory.create(serverUrl)
        val contactList = dbManager.contactsDao().getAllSync()

        ui {
            (activity as MainActivity).contactsLoadingState.observe(viewLifecycleOwner, Observer { state ->
                when (state) {
                    is ContactsLoadingState.Loading -> {
                        if (contactArrayList.isEmpty() && contactList.isEmpty()) {
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

    private fun setupFrameLayout(filteredContactArrayList: ArrayList<Contact>, spotlightResult: ArrayList<ItemHolder<*>>? = null) {
        loadedOnce = true
        if (filteredContactArrayList.size == 0 && ((spotlightResult == null) or (spotlightResult?.size == 0))) {
            emptyTextView!!.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyTextView!!.visibility = View.GONE
            setUpNewList(map(filteredContactArrayList, spotlightResult))
        }
    }

    private fun setupFrameLayout(spotlightResult: ArrayList<ItemHolder<*>>? = null) {
        setUpNewList(map(spotlightResult))
    }

    private fun setUpNewList(list: ArrayList<ItemHolder<*>> = finalList) {
        recyclerView.visibility = View.VISIBLE
        finalList.clear()
        finalList.addAll(list)
        contactsSelectionTracker?.clearSelection()
        if (enableGroups)
            contactsSelectionTracker?.select(-1)
        contactsAdapter.notifyDataSetChanged()
        selectedContactsAdapter.notifyDataSetChanged()
        selectedContacts.forEach {
            addToSelectionTracker(it)
        }
    }

    private fun map(contacts: List<Contact>, spotlightResult: ArrayList<ItemHolder<*>>? = null): ArrayList<ItemHolder<*>> {
        val finalList = ArrayList<ItemHolder<*>>(contacts.size + 2)
        val userList = ArrayList<ItemHolder<*>>(contacts.size)
        val userContactList: ArrayList<Contact> = ArrayList()
        val unfilteredContactsList: ArrayList<Contact> = ArrayList()
        val contactsList = ArrayList<ItemHolder<*>>(contacts.size)
        val usernameSet = mutableListOf<String>()

        contacts.forEach { contact ->
            if(contact.getUsername()!= null){
                // Users in their own list for filtering before adding to an ItemHolder
                userContactList.add(contact)
                usernameSet.add(contact.getUsername()!!)
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
        if(contactsList.size > 0) {
            finalList.add(ContactsHeaderItemHolder(getString(R.string.Invite_contacts)))
            finalList.addAll(contactsList)
        }
        if(spotlightResult !== null && spotlightResult.size >0) {
            finalList.add(ContactsHeaderItemHolder(getString(R.string.Spotlight_Result)))
            spotlightResult.forEach { item ->
                val username = (item.data as Contact).getUsername()
                if((username == null) || (!usernameSet.contains(username))) {
                    finalList.add(item)
                }
            }
        }
        finalList.add(InviteItemHolder("invite"))
        return finalList
    }

    // Contacts access permission not granted yet
    private fun map(spotlightResult: ArrayList<ItemHolder<*>>? = null): ArrayList<ItemHolder<*>> {
        val finalList = ArrayList<ItemHolder<*>>(3)
        if (spotlightResult !== null) {
            finalList.add(ContactsHeaderItemHolder(getString(R.string.Spotlight_Result)))
            spotlightResult.forEach { item ->
                finalList.add(item)
            }
            finalList.add(InviteItemHolder("invite"))
        } else {
            finalList.add(ContactsHeaderItemHolder(getString(R.string.Invite_contacts)))
            finalList.add(PermissionsItemHolder("request_permissions"))
            finalList.add(InviteItemHolder("invite"))
        }
        return finalList
    }

    private fun setupTrackerAndFab() {

        contactsSelectionTracker = SelectionTracker.Builder<Long>(
                "contactsSelection",
                recyclerView,
                ContactsItemKeyProvider(recyclerView),
                ContactsItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                object : SelectionTracker.SelectionPredicate<Long>() {
                    override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
                        val position = key.toInt()
                        if (position == -1) return true
                        if (finalList[position] is ContactsItemHolder &&
                                (finalList[position] as ContactsItemHolder).data.getUsername() != null) {
                            val contact = (finalList[position] as ContactsItemHolder).data
                            if (nextState)
                                addToSelectedContact(contact)
                            else
                                removeFromSelectedContact(contact)
                            return true
                        }
                        return false
                    }
                    override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
                        return true
                    }
                    override fun canSelectMultiple(): Boolean {
                        return true
                    }
                }
        ).build()

        fab = view?.findViewById(R.id.contacts_action_fab)
        fab?.isVisible = enableGroups
        fab?.setOnClickListener { view ->
            if (selectedContacts.size < 1) {
                showToast("Select at least one contact")
            } else {
                val createChannelFragment = CreateChannelFragment.newInstance(selectedContacts)
                val transaction = activity?.supportFragmentManager?.beginTransaction()
                transaction?.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                transaction?.replace(this.id, createChannelFragment, "createChannelFragment")
                transaction?.addToBackStack("createChannelFragment")?.commit()
            }
        }

        contactsSelectionTracker?.addObserver(
                object : SelectionTracker.SelectionObserver<Long>() {
                    override fun onSelectionChanged() {
                        super.onSelectionChanged()
                        var items = contactsSelectionTracker?.selection!!.size()
                        if (contactsSelectionTracker?.isSelected(-1) == true)
                            items--
                        val showSelection = (items > 0) or (selectedContacts.size > 0)
                        onSelectionChanged(showSelection)
                    }
                })

        contactsAdapter.contactsSelectionTracker = contactsSelectionTracker
    }

    private fun onSelectionChanged(showSelection: Boolean) {
        fab?.isVisible = showSelection
        selectedContactsRecyclerView.isVisible = showSelection
        selected_contacts_divider?.isVisible = showSelection
    }

    private fun indexOfSelectedContact(username: String) : Int {
        selectedContacts.forEachIndexed { index, contact ->
            if (contact.getUsername() == username)
                return index
        }
        return -1
    }

    private fun addToSelectedContact(contact: Contact) {
        if (indexOfSelectedContact(contact.getUsername()!!) == -1) {
            val index = selectedContacts.size
            selectedContacts.add(index, contact)
            selectedContactsAdapter.notifyItemInserted(index)
        }
    }

    private fun removeFromSelectedContact(contact: Contact) {
        val index = indexOfSelectedContact(contact.getUsername()!!)
        if (index != -1) {
            selectedContacts.removeAt(index)
            selectedContactsAdapter.notifyItemRemoved(index)
        }
    }

    private fun addToSelectionTracker(contact: Contact) {
        val username = contact.getUsername()
        finalList.forEachIndexed { index, item ->
            if (item is ContactsItemHolder && item.data.getUsername() == username) {
                contactsSelectionTracker?.select(index.toLong())
                return
            }
        }
    }

    private fun removeFromSelectionTracker(contact: Contact) {
        val username = contact.getUsername()
        finalList.forEachIndexed { index, item ->
            if (item is ContactsItemHolder && item.data.getUsername() == username) {
                contactsSelectionTracker?.deselect(index.toLong())
                return
            }
        }
    }

    private fun showSpinner() {
        try {
            ui {
                (activity as MainActivity).toolbar.toolbar_progress_bar?.visibility = VISIBLE
            }
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    private fun hideSpinner() {
        try {
            ui {
                (activity as MainActivity).toolbar.toolbar_progress_bar?.visibility = GONE
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

class ContactsItemKeyProvider(private val recyclerView: RecyclerView) :
        ItemKeyProvider<Long>(ItemKeyProvider.SCOPE_MAPPED) {

    override fun getKey(position: Int): Long? {
        return recyclerView.adapter?.getItemId(position)
    }

    override fun getPosition(key: Long): Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}
