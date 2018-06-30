package chat.rocket.android.settings.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import chat.rocket.android.R
import chat.rocket.android.about.ui.AboutFragment
import chat.rocket.android.settings.password.ui.PasswordActivity
import chat.rocket.android.settings.presentation.SettingsView
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlin.reflect.KClass

class SettingsFragment : Fragment(), SettingsView, AdapterView.OnItemClickListener {
    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_settings)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListView()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.getItemAtPosition(position).toString()) {
            resources.getString(R.string.title_password) -> {
                startNewActivity(PasswordActivity::class)
            }
            resources.getString(R.string.title_about) -> {
                (activity as AppCompatActivity).addFragmentBackStack("AboutFragmnet", R.id.fragment_container){
                    AboutFragment.newInstance()
                }
            }
        }
    }

    private fun setupListView() {
        settings_list.onItemClickListener = this
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)?.supportActionBar?.title =
                getString(R.string.title_settings)
    }

    private fun startNewActivity(classType: KClass<out AppCompatActivity>) {
        startActivity(Intent(activity, classType.java))
        activity?.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }
}
