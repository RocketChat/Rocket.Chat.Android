package chat.rocket.android.createChannel.addMembers.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.createChannel.addMembers.presentation.AddMembersPresenter
import chat.rocket.android.createChannel.addMembers.presentation.AddMembersView
import com.jakewharton.rxbinding2.widget.RxTextView
import kotlinx.android.synthetic.main.activity_create_new_channel.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import javax.inject.Inject

class AddMembersActivity : AppCompatActivity(), AddMembersView {
    override fun showLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hideLoading() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(resId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showMessage(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showGenericErrorMessage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Inject
    lateinit var presenter: AddMembersPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpToolBar()
    }

    private fun setUpToolBar(){
        toolbar_title.text = getString(R.string.title_add_members)
        toolbar_action_text.text = getString(R.string.action_select_members)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        RxTextView.textChanges(channel_name_edit_text).subscribe { text ->
            toolbar_action_text.isEnabled = text.isNotEmpty()
            if (text.isEmpty())
                toolbar_action_text.alpha = 0.8f
            else
                toolbar_action_text.alpha = 1.0f
        }
    }
}