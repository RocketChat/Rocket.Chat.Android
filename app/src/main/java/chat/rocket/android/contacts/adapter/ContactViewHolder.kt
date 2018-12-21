package chat.rocket.android.contacts.adapter

import android.content.res.Resources
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import chat.rocket.android.chatrooms.adapter.ViewHolder
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
                chat_username.isGone=true
            }else{
                invite_contact.isGone= true
                chat_username.isVisible=true
                chat_username.text= "@"+contact!!.getUsername()
            }

            if (contact!!.isPhone()) {
                contact_detail.text = contact!!.getPhoneNumber()
            } else {
                contact_detail.text = contact!!.getEmailAddress()
            }
        }
    }


}