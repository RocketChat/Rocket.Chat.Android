package chat.rocket.android.contacts.adapter

import android.content.res.Resources
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import chat.rocket.android.chatrooms.adapter.ViewHolder
import chat.rocket.android.contacts.adapter.ContactItemHolder
import kotlinx.android.synthetic.main.item_contact.view.*

class ContactViewHolder(itemView: View) : ViewHolder<ContactItemHolder>(itemView) {

    private val resources: Resources = itemView.resources


    override fun bindViews(data: ContactItemHolder) {
        val contact = data.data
        with(itemView) {
            contact_image_avatar.setImageURI(contact.getAvatarUrl())
            contact_name.text = contact.getName()

            if (contact.getUsername()==null) {
                invite_contact.isVisible = true
              text_email.isGone=true
            }else{
                invite_contact.isGone= true
                text_email.isVisible=true
                text_email.text= "@"+contact!!.getUsername()
            }

            if (contact!!.isPhone()) {
                phone_number.text = contact!!.getPhoneNumber()
            } else {
                phone_number.text = contact!!.getEmailAddress()
            }

            invite_contact.setOnClickListener{
               // it.context.startActivity(Intent.createChooser(this, context.getString(R.string.msg_share_using)))

            }

            setOnClickListener {

            }
        }
    }


}