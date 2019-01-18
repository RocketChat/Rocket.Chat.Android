package chat.rocket.android.members.adapter

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatroom.uimodel.MessageUiModel
import chat.rocket.android.members.ui.GroupMemberBottomSheet
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.toList
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_member.view.*


class ViewHolder(
        itemView: View,
        private val listener: ActionsListener
) : RecyclerView.ViewHolder(itemView), MenuItem.OnMenuItemClickListener {
    var data: MemberUiModel? = null

//    init {
//            setupActionMenu(itemView)
//    }

    fun bind(memberUiModel: MemberUiModel, listener: (MemberUiModel) -> Unit) = with(itemView) {
        data = memberUiModel
        image_avatar.setImageURI(memberUiModel.avatarUri)
        text_member.content = memberUiModel.displayName
        text_member.setCompoundDrawablesRelativeWithIntrinsicBounds(DrawableHelper.getUserStatusDrawable(memberUiModel.status, context), null, null, null)
        text_member_owner.content = "Owner"
        text_member_leader.content = "Leader"
        text_member_moderator.content = "Mod"
        text_member_owner.isVisible =memberUiModel.roles?.contains("owner") == true
        text_member_leader.isVisible = memberUiModel.roles?.contains("leader") == true
        text_member_moderator.isVisible = memberUiModel.roles?.contains("moderator") == true
        setOnClickListener { listener(memberUiModel) }
        setupActionMenu(itemView)
    }

    interface ActionsListener {
        fun isActionsEnabled(): Boolean
        fun onActionSelected(item: MenuItem, member: MemberUiModel)
    }

    internal fun setupActionMenu(view: View) {
        view.setOnLongClickListener{
                data?.let {
                        var menuItems = view.context.inflate(R.menu.group_member_actions).toList()
//                        if(!isRoomOwner) menuItems = menuItems.filter { it.itemId == R.id.action_member_mute || it.itemId == R.id.action_member_ignore }
                        menuItems.find { it.itemId == R.id.action_member_set_owner }?.apply {
                            if (it.roles?.contains("owner") == true) title = "Remove as Owner"
                        }
                        menuItems.find { it.itemId == R.id.action_member_set_leader }?.apply {
                            if (it.roles?.contains("leader") == true) title = "Remove as Leader"
                        }
                        menuItems.find { it.itemId == R.id.action_member_set_moderator }?.apply {
                            if (it.roles?.contains("moderator") == true) title = "Remove as Moderator"
                        }
                        menuItems.find { it.itemId == R.id.action_member_mute }?.apply {
                            if (it.muted) {
                                title = "Unmute user"
                                setIcon(R.drawable.ic_mic_on_24dp)
                            }
                        }
                        menuItems.find { it.itemId == R.id.action_member_ignore }?.apply {
//                            if (it.roles.contains("owner")) title = "Remove Owner"
                        }
                        menuItems.find { it.itemId == R.id.action_member_remove }?.apply {
//                            if (it.roles.contains("owner")) title = "Remove Owner"
                        }
                        view.context?.let {
                            if (it is ContextThemeWrapper && it is AppCompatActivity) {
                                with(it) {
                                    val actionsBottomSheet = GroupMemberBottomSheet()
                                    actionsBottomSheet.addItems(menuItems, this@ViewHolder)
                                    actionsBottomSheet.show(supportFragmentManager, null)
                                }
                            }
                        }
                    }
            true
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        data?.let {
            listener.onActionSelected(item, it)
        }
        return true
    }
}


