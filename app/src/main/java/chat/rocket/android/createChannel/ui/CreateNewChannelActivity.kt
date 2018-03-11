package chat.rocket.android.createChannel.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import chat.rocket.android.R
import chat.rocket.android.createChannel.presentation.CreateNewChannelPresenter
import chat.rocket.android.createChannel.presentation.CreateNewChannelView
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_create_new_channel.*
import javax.inject.Inject


class CreateNewChannelActivity : AppCompatActivity(), CreateNewChannelView {
    @Inject
    lateinit var presenter: CreateNewChannelPresenter

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.home -> {
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
        supportActionBar?.title = getString(R.string.title_create_new_channel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        RxTextView.textChanges(channel_name_edit_text).subscribe { text ->
            create_channel_action_text.isEnabled = text != ""
            if (text != "")
                create_channel_action_text.alpha = 1.0f
            else
                create_channel_action_text.alpha = 0.5f
        }
    }

    private fun setUpOnClickListeners(){
        public_channel.setOnClickListener{
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

        private_channel.setOnClickListener{
            channel_type.text = getString(R.string.private_channel_type)
            channel_description.text = getString(R.string.public_channel_description)
            placeholder.setImageDrawable(getDrawable(R.drawable.ic_room_lock))
            (getDrawable(R.drawable.button_border) as GradientDrawable).setColor(resources.getColor(R.color.red))
            (getDrawable(R.drawable.button_solid) as GradientDrawable).setColor(resources.getColor(R.color.colorLightTheme))
            private_channel.background = getDrawable(R.drawable.button_border)
            public_channel.background = getDrawable(R.drawable.button_solid)
            private_channel.setTextColor(resources.getColor(R.color.colorLightTheme))
            public_channel.setTextColor(resources.getColor(R.color.red))
        }
    }
}