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
    private var contactArrayList: ArrayList<Contact?>? = null

    /**
     *  The mapping of contacts with their registration status
     */
    private var contactHashMap: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments
        if (bundle != null) {
            contactArrayList = bundle.getParcelableArrayList("CONTACT_ARRAY_LIST")
            contactHashMap= bundle.getSerializable("CONTACT_HASH_MAP") as HashMap<String, String>
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        val context = view.context

        val recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
        val emptyTextView = view.findViewById(R.id.text_no_data_to_display) as TextView

        if (contactArrayList!!.size == 0) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = ContactRecyclerViewAdapter(context, contactArrayList!!, contactHashMap)
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
