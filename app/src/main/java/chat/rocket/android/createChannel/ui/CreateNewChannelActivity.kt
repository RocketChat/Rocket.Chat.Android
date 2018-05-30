package chat.rocket.android.createChannel.ui

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.design.chip.Chip
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import chat.rocket.android.R
import chat.rocket.android.createChannel.addMembers.ui.AddMembersActivity
import chat.rocket.android.createChannel.presentation.CreateNewChannelPresenter
import chat.rocket.android.createChannel.presentation.CreateNewChannelView
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.showToast
import chat.rocket.common.model.RoomType
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_create_new_channel.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import javax.inject.Inject

internal const val ADD_MEMBERS_ACTIVITY_REQUEST_CODE = 1

class CreateNewChannelActivity : AppCompatActivity(), CreateNewChannelView {
    @Inject
    lateinit var presenter: CreateNewChannelPresenter
    private var channelType: RoomType = RoomType.CHANNEL
    private var listOfUsers: ArrayList<String> = ArrayList()
    private lateinit var observableForToolbarAction: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_channel)
        setUpToolBar()
        setUpOnClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        observableForToolbarAction.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ADD_MEMBERS_ACTIVITY_REQUEST_CODE && data != null) {
                listOfUsers = data.getStringArrayListExtra("members")
                selected_members_chips.removeAllViews()
                refreshMembersChips()
            }
        }
    }

    override fun showLoading() {
        view_loading.setVisible(true)
        layout_container.alpha = 0.5f
        layout_container.isEnabled = false
    }

    override fun hideLoading() {
        view_loading.setVisible(false)
        layout_container.alpha = 1.0f
        layout_container.isEnabled = true
    }

    override fun showChannelCreatedSuccessfullyMessage() {
        showToast(getString(R.string.msg_channel_created_successfully))
        finish()
    }

    override fun showMessageAndClearText(resId: Int) {
        channel_name_edit_text.setText("")
        showToast(getString(resId))
    }

    override fun showMessageAndClearText(message: String) {
        channel_name_edit_text.setText("")
        showToast(message)
    }

    override fun showErrorMessage() {
        showMessageAndClearText(getString(R.string.msg_generic_error))
    }


    private fun refreshMembersChips() {
        for (element in listOfUsers) {
            val memberChip = Chip(this)
            memberChip.chipText = element
            memberChip.isCloseIconEnabled = false
            memberChip.isLongClickable = false
            memberChip.setChipBackgroundColorResource(R.color.icon_grey)
            selected_members_chips.addView(memberChip)
        }
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.title_create_new_channel)
        toolbar_action_text.text = getString(R.string.action_create_new_channel)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        observableForToolbarAction =
                RxTextView.textChanges(channel_name_edit_text).subscribe { text ->
                    toolbar_action_text.isEnabled = (text.isNotEmpty() && listOfUsers.isNotEmpty())
                    if (text.isEmpty()) {
                        toolbar_action_text.alpha = 0.8f
                    } else {
                        toolbar_action_text.alpha = 1.0f
                    }
                }
    }

    private fun setUpOnClickListeners() {
        public_channel.setOnClickListener {
            channelType = RoomType.CHANNEL

            channel_type.text = getString(R.string.public_channel_type)
            channel_description.text = getString(R.string.public_channel_description)

            placeholder.setImageDrawable(getDrawable(R.drawable.ic_hashtag_black_12dp))

            (getDrawable(R.drawable.button_border) as GradientDrawable).setColor(
                resources.getColor(
                    R.color.default_background
                )
            )
            (getDrawable(R.drawable.button_solid) as GradientDrawable).setColor(resources.getColor(R.color.colorRed))

            private_channel.background = getDrawable(R.drawable.button_border)
            public_channel.background = getDrawable(R.drawable.button_solid)

            private_channel.setTextColor(resources.getColor(R.color.colorRed))
            public_channel.setTextColor(resources.getColor(R.color.default_background))
        }

        private_channel.setOnClickListener {
            channelType = RoomType.PRIVATE_GROUP

            channel_type.text = getString(R.string.private_channel_type)
            channel_description.text = getString(R.string.private_channel_type_description)

            placeholder.setImageDrawable(getDrawable(R.drawable.ic_lock_black_12_dp))

            (getDrawable(R.drawable.button_border) as GradientDrawable).setColor(
                resources.getColor(R.color.colorRed)
            )
            (getDrawable(R.drawable.button_solid) as GradientDrawable).setColor(
                resources.getColor(R.color.default_background)
            )
            (getDrawable(R.drawable.button_solid) as GradientDrawable).setStroke(
                1,
                resources.getColor(R.color.colorRed)
            )

            private_channel.background = getDrawable(R.drawable.button_border)
            public_channel.background = getDrawable(R.drawable.button_solid)

            private_channel.setTextColor(resources.getColor(R.color.default_background))
            public_channel.setTextColor(resources.getColor(R.color.colorRed))
        }

        toolbar_action_text.setOnClickListener {
            if (toolbar_action_text.isEnabled) {
                presenter.createNewChannel(
                    channelType,
                    channel_name_edit_text.text.toString(),
                    listOfUsers,
                    false
                )
            }
        }

        add_members_view.setOnClickListener {
            val intent = Intent(this, AddMembersActivity::class.java)
            intent.putExtra("chips", listOfUsers)
            startActivityForResult(intent, ADD_MEMBERS_ACTIVITY_REQUEST_CODE)
        }
    }
}