package chat.rocket.android.create_channel.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import chat.rocket.android.R
import chat.rocket.android.create_channel.presentation.CreateNewChannelPresenter
import chat.rocket.android.create_channel.presentation.CreateNewChannelView
import dagger.android.AndroidInjection
import javax.inject.Inject

class CreateNewChannelActivity : AppCompatActivity(), CreateNewChannelView {
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.home -> {
                finish()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    @Inject
    var presenter: CreateNewChannelPresenter = CreateNewChannelPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_channel)
        setUpToolBar()
    }

    private fun setUpToolBar() {
        supportActionBar?.title = getString(R.string.title_create_new_channel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}