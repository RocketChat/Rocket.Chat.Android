package chat.rocket.android.createChannel.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.createChannel.presentation.CreateNewChannelPresenter
import chat.rocket.android.createChannel.presentation.CreateNewChannelView
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.common.model.roomTypeOf
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_create_new_channel.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import javax.inject.Inject


class CreateNewChannelActivity : AppCompatActivity(), CreateNewChannelView {
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

    override fun showMessage(resId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showGenericErrorMessage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Inject
    lateinit var presenter: CreateNewChannelPresenter
    private var channelType: String = "public"

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_channel)
        setUpToolBar()
        setUpOnClickListeners()
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        toolbar_title.text = getString(R.string.title_create_new_channel)
        toolbar_action_text.text = getString(R.string.action_create_new_channel)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        RxTextView.textChanges(channel_name_edit_text).subscribe { text ->
            toolbar_action_text.isEnabled = text.isNotEmpty()
            if (text.isEmpty()) {
                toolbar_action_text.alpha = 0.8f
            } else {
                toolbar_action_text.alpha = 1.0f
            }
        }
    }

    private fun setUpOnClickListeners() {
        public_channel.setOnClickListener {
            channelType = "c"

            channel_type.text = getString(R.string.public_channel_type)
            channel_description.text = getString(R.string.private_channel_type_description)

            placeholder.setImageDrawable(getDrawable(R.drawable.ic_hashtag_black))

            (getDrawable(R.drawable.button_border) as GradientDrawable).setColor(resources.getColor(R.color.colorLightTheme))
            (getDrawable(R.drawable.button_solid) as GradientDrawable).setColor(resources.getColor(R.color.red))

            private_channel.background = getDrawable(R.drawable.button_border)
            public_channel.background = getDrawable(R.drawable.button_solid)

            private_channel.setTextColor(resources.getColor(R.color.red))
            public_channel.setTextColor(resources.getColor(R.color.colorLightTheme))
        }

        private_channel.setOnClickListener {
            channelType = "p"

            channel_type.text = getString(R.string.private_channel_type)
            channel_description.text = getString(R.string.public_channel_description)

            placeholder.setImageDrawable(getDrawable(R.drawable.ic_room_lock))

            (getDrawable(R.drawable.button_border) as GradientDrawable).setColor(resources.getColor(R.color.red))
            (getDrawable(R.drawable.button_solid) as GradientDrawable).setColor(resources.getColor(R.color.colorLightTheme))
            (getDrawable(R.drawable.button_solid) as GradientDrawable).setStroke(1, resources.getColor(R.color.red))

            private_channel.background = getDrawable(R.drawable.button_border)
            public_channel.background = getDrawable(R.drawable.button_solid)

            private_channel.setTextColor(resources.getColor(R.color.colorLightTheme))
            public_channel.setTextColor(resources.getColor(R.color.red))
        }

        toolbar_action_text.setOnClickListener {
            if (toolbar_action_text.isEnabled) {
                presenter.createNewChannel(roomTypeOf(channelType), channel_name_edit_text.text.toString(), listOf("aniket03"), false)
            }
        }
    }
}