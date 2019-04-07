package chat.rocket.android.members.adapter

import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.members.ui.GroupMemberBottomSheet
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.util.extensions.content
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.toList
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_member.view.*


class ViewHolder(
        itemView: View,
        private val listener: ActionsListener,
        private val isOwner: Boolean,
        private val isMod: Boolean
) : RecyclerView.ViewHolder(itemView), MenuItem.OnMenuItemClickListener {
    var data: MemberUiModel? = null
    var index: Int = 0

    fun bind(memberUiModel: MemberUiModel, position: Int, listener: (MemberUiModel) -> Unit) = with(itemView) {
        data = memberUiModel
        index = position
        image_avatar.setImageURI(memberUiModel.avatarUri)
        text_member.content = memberUiModel.displayName
        text_member.setCompoundDrawablesRelativeWithIntrinsicBounds(DrawableHelper.getUserStatusDrawable(memberUiModel.status, context), null, null, null)
        text_member_owner.setText(R.string.owner)
        text_member_leader.setText(R.string.leader)
        text_member_moderator.setText(R.string.moderator)
        text_member_owner.isVisible =memberUiModel.roles?.contains("owner") == true
        text_member_leader.isVisible = memberUiModel.roles?.contains("leader") == true
        text_member_moderator.isVisible = memberUiModel.roles?.contains("moderator") == true
        setOnClickListener { listener(memberUiModel) }
        setupActionMenu(itemView)
    }

    interface ActionsListener {
        fun isActionsEnabled(): Boolean
        fun onActionSelected(item: MenuItem, member: MemberUiModel, index: Int)
    }

    internal fun setupActionMenu(view: View) {
        view.setOnLongClickListener{
                data?.let {
                        var menuItems = view.context.inflate(R.menu.group_member_actions).toList()
                        if (!isOwner && !isMod)
                            menuItems = menuItems.filter { it.itemId == R.id.action_member_mute }
                        else if (!isOwner && isMod)
                            menuItems = menuItems.filter { it.itemId == R.id.action_member_mute || it.itemId == R.id.action_member_remove}
                    menuItems.find { it.itemId == R.id.action_member_set_owner }?.apply {
                            if (it.roles?.contains("owner") == true) setTitle(R.string.action_remove_owner)
                        }
                        menuItems.find { it.itemId == R.id.action_member_set_leader }?.apply {
                            if (it.roles?.contains("leader") == true) setTitle(R.string.action_remove_leader)
                        }
                        menuItems.find { it.itemId == R.id.action_member_set_moderator }?.apply {
                            if (it.roles?.contains("moderator") == true) setTitle(R.string.action_remove_moderator)
                        }
                        menuItems.find { it.itemId == R.id.action_member_mute }?.apply {
                            if (it.muted) {
                                setTitle(R.string.action_unmute_user)
                                setIcon(R.drawable.ic_mic_on_24dp)
                            }
                        }
//                    TODO: Check why ignore is not working
//                        menuItems.find { it.itemId == R.id.action_member_ignore }?.apply {
//                            if (it.roles.contains("owner")) title = "Remove Owner"
//                        }
                    view.context?.let {
                            if (it is ContextThemeWrapper && it.baseContext is AppCompatActivity) {
                                with(it.baseContext as AppCompatActivity) {
                                    if (this.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                        val actionsBottomSheet = GroupMemberBottomSheet()
                                        actionsBottomSheet.addItems(menuItems, this@ViewHolder)
                                        actionsBottomSheet.show(supportFragmentManager, null)
                                    }
                                }
                            }
                        }
                    }
            true
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        data?.let {
            listener.onActionSelected(item, it, index)
        }
        return true
    }
}


