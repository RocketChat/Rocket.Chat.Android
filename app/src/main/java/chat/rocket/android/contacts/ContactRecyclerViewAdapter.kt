package chat.rocket.android.contacts

import android.content.Context
import timber.log.Timber
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.contacts.models.Contact
import java.util.*
import kotlin.collections.HashMap

class ContactRecyclerViewAdapter(
        private val context: Context,
        private val contactArrayList: ArrayList<Contact?>,
        private val contactHashMap: HashMap<String, String>
) : RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return contactArrayList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.contact = contactArrayList[position]
        holder.status = contactHashMap.get(holder.contact!!.getPhoneNumber())
        try {
            holder.contactName.text = holder.contact!!.getName()
            if (holder.contact!!.isPhone()) {
                holder.contactDetail.text = holder.contact!!.getPhoneNumber()
            } else {
                holder.contactDetail.text = holder.contact!!.getEmailAddress()
            }
        } catch (exception: NullPointerException) {
            Timber.e("Failed to send resolution. Exception is: $exception")
        }

    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var contact: Contact? = null
        var status: String? = null

        var contactName: TextView
        var contactDetail: TextView
        var inviteButton: Button

        init {
            this.contactName = view.findViewById(R.id.contact_name) as TextView
            this.contactDetail = view.findViewById(R.id.contact_detail) as TextView
            this.inviteButton = view.findViewById(R.id.invite_contact) as Button

            this.inviteButton.setOnClickListener { view ->
                run {
                    Toast.makeText(
                            context,
                            "${contact!!.getName()!!}: ${contactDetail.text}",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
