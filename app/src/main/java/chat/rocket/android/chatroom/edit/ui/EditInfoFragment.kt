package chat.rocket.android.chatroom.edit.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.chatroom.edit.presentation.EditInfoPresenter
import chat.rocket.android.chatroom.edit.presentation.EditInfoView
import chat.rocket.android.util.extensions.asObservable
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.core.model.ChatRoom
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import dagger.android.support.AndroidSupportInjection
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.fragment_chat_room_edit_info.*
import javax.inject.Inject

fun newInstance(chatRoomId: String, chatRoomType: String): Fragment {
    return EditInfoFragment().apply {
        arguments = Bundle(1).apply {
            putString(BUNDLE_CHAT_ROOM_ID, chatRoomId)
            putString(BUNDLE_CHAT_ROOM_TYPE, chatRoomType)
        }
    }
}

private const val BUNDLE_CHAT_ROOM_ID = "chat_room_id"
private const val BUNDLE_CHAT_ROOM_TYPE = "chat_room_type"

private const val PRIVATE = "p"
private const val PUBLIC = "c"

class EditInfoFragment: Fragment(), EditInfoView {
    @Inject lateinit var presenter: EditInfoPresenter

    private lateinit var chatRoomId: String
    private lateinit var chatRoomType: String

    private lateinit var chatRoom: ChatRoom

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        val bundle = arguments

        if (bundle != null) {
            chatRoomId = bundle.getString(BUNDLE_CHAT_ROOM_ID)
            chatRoomType = bundle.getString(BUNDLE_CHAT_ROOM_TYPE)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return container?.inflate(R.layout.fragment_chat_room_edit_info)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter.loadRoomInfo(chatRoomId)
    }

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
       showToast(message)
    }

    override fun showGenericErrorMessage() {
        showToast(R.string.msg_generic_error)
    }

    override fun showLoading() {
        progress?.show()
        room_info_edit_layout.visibility = View.GONE
    }

    override fun hideLoading() {
        progress?.hide()
        room_info_edit_layout.visibility = View.VISIBLE
    }

    override fun showRoomInfo(room: ChatRoom) {
        if (room.name != null)
            room_name_edit_text.text = SpannableStringBuilder(room.name)

        if (room.topic != null)
            room_topic_edit_text.text = SpannableStringBuilder(room.topic)

        if (room.announcement != null)
            room_announcement_edit_text.text = SpannableStringBuilder(room.announcement)

        if (room.description != null)
            room_description_edit_text.text = SpannableStringBuilder(room.description)

        setupSwitches(room.type.toString(), room.readonly, room.archived)
        setupButtons()
        watchForChanges()
    }

    private fun setupButtons() {
        cancel_button.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        reset_button.setOnClickListener {

        }
        save_button.setOnClickListener {
            presenter.saveChatInformation(
                    chatRoomId,
                    chatRoomType,
                    room_name_edit_text.text.toString(),
                    room_topic_edit_text.text.toString(),
                    room_announcement_edit_text.text.toString(),
                    room_description_edit_text.text.toString(),
                    booleanTypeToString(room_type_switch.isChecked),
                    room_ro_or_collab_switch.isChecked,
                    room_archived_switch.isChecked,
                    chatRoom
            )
        }
    }

    override fun onRoomUpdate(room: ChatRoom) {
        this.chatRoomId = room.id
        this.chatRoomType = room.type.toString()
    }

    private fun setupSwitches(type: String, readOnly: Boolean?, archived: Boolean) {
        if (type == PRIVATE)
            room_type_switch.isChecked = true

        if (readOnly != null && readOnly)
            room_ro_or_collab_switch.isChecked = true

        if (archived)
            room_archived_switch.isChecked = true
    }

    private fun setTypeTextColors(colorPublic: Int, colorPrivate: Int) {
        room_public_title.setTextColor(colorPublic)
        room_public_info.setTextColor(colorPublic)
        room_private_title.setTextColor(colorPrivate)
        room_private_info.setTextColor(colorPrivate)
    }

    private fun setRoCollabTextColors(colorCollab: Int, colorRo: Int) {
        room_collab_title.setTextColor(colorCollab)
        room_collab_info.setTextColor(colorCollab)
        room_read_only_title.setTextColor(colorRo)
        room_read_only_info.setTextColor(colorRo)
    }

    private fun watchForChanges() {
        Observables.combineLatest(
                room_name_edit_text.asObservable(),
                room_topic_edit_text.asObservable(),
                room_announcement_edit_text.asObservable(),
                room_description_edit_text.asObservable(),
                RxCompoundButton.checkedChanges(room_type_switch),
                RxCompoundButton.checkedChanges(room_ro_or_collab_switch),
                RxCompoundButton.checkedChanges(room_archived_switch)) { name, topic, announcement,
                                                                         description, type,
                                                                         ro_or_collab, archived ->
            return@combineLatest (name.toString() != chatRoom.name || topic.toString() !=
                    chatRoom.topic || announcement.toString() != chatRoom.announcement ||
                    description.toString() != chatRoom.description || booleanTypeToString(type)
                    != chatRoom.type.toString() || ro_or_collab != chatRoom.readonly || archived
                    != chatRoom.archived)
        }.subscribe({ hasChanged ->
            if (hasChanged) {
                save_button.isEnabled = true
                reset_button.isEnabled = true
            } else {
                save_button.isEnabled = false
                reset_button.isEnabled = false
            }

            if (room_ro_or_collab_switch.isChecked)
                setRoCollabTextColors(ContextCompat.getColor(context!!, R.color.darkGray),
                        ContextCompat.getColor(context!!, R.color.colorPrimary))
            else
                setRoCollabTextColors(ContextCompat.getColor(context!!, R.color.colorPrimary),
                        ContextCompat.getColor(context!!, R.color.darkGray))

            if (room_type_switch.isChecked)
                setTypeTextColors(ContextCompat.getColor(context!!, R.color.darkGray),
                        ContextCompat.getColor(context!!, R.color.colorPrimary))
            else
                setTypeTextColors(ContextCompat.getColor(context!!, R.color.colorPrimary),
                        ContextCompat.getColor(context!!, R.color.darkGray))
        })
    }

    private fun booleanTypeToString(type: Boolean): String = if (type) PRIVATE else PUBLIC
}