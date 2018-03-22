package chat.rocket.android.settings.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.settings.password.ui.PasswordActivity
import chat.rocket.android.settings.presentation.SettingsView
import chat.rocket.android.util.extensions.inflate
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlin.reflect.KClass

class SettingsFragment: Fragment(), SettingsView,View.OnClickListener{
    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_settings)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListener()
        setVersion()
    }

    private fun setVersion() {
        val version = resources.getString(R.string.version)+" " + BuildConfig.VERSION_NAME
        text_version_name.text = version
    }

    private fun setupListener() {
        text_change_password.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.text_change_password -> startNewActivity(PasswordActivity::class)
        }
    }

    private fun setupToolbar() {
        (activity as MainActivity).toolbar.title = getString(R.string.title_settings)
    }

    private fun startNewActivity(classType: KClass<out AppCompatActivity>) {
        startActivity(Intent(activity, classType.java))
        activity?.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }
}
