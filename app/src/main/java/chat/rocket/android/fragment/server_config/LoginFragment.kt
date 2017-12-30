package chat.rocket.android.fragment.server_config

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.api.MethodCallHelper
import chat.rocket.android.layouthelper.oauth.OAuthProviderInfo
import chat.rocket.android.log.RCLog
import chat.rocket.core.models.LoginServiceConfiguration
import chat.rocket.persistence.realm.repositories.RealmLoginServiceConfigurationRepository
import chat.rocket.persistence.realm.repositories.RealmPublicSettingRepository
import com.jakewharton.rxbinding2.widget.RxTextView
import java.util.*


/**
 * Login screen.
 */
class LoginFragment : AbstractServerConfigFragment(), LoginContract.View {

    private lateinit var presenter: LoginContract.Presenter
    private lateinit var container: ConstraintLayout
    private lateinit var waitingView: View
    private lateinit var txtUsername: TextView
    private lateinit var txtPasswd: TextView
    private lateinit var textInputUsername: TextInputLayout
    private lateinit var textInputPassword: TextInputLayout

    override fun getLayout(): Int {
        return R.layout.fragment_login
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = LoginPresenter(
                RealmLoginServiceConfigurationRepository(hostname),
                RealmPublicSettingRepository(hostname),
                MethodCallHelper(context, hostname)
        )
    }

    override fun onSetupView() {
        container = rootView.findViewById(R.id.container)

        val btnEmail = rootView.findViewById<Button>(R.id.btn_login_with_email)
        val btnUserRegistration = rootView.findViewById<Button>(R.id.btn_user_registration)
        txtUsername = rootView.findViewById(R.id.editor_username)
        txtPasswd = rootView.findViewById(R.id.editor_passwd)
        textInputUsername = rootView.findViewById(R.id.text_input_username)
        textInputPassword = rootView.findViewById(R.id.text_input_passwd)

        setUpRxBinders()

        waitingView = rootView.findViewById(R.id.waiting)

        btnEmail.setOnClickListener { _ -> presenter.login(txtUsername.text.toString(), txtPasswd.text.toString()) }

        btnUserRegistration.setOnClickListener { _ ->
            UserRegistrationDialogFragment.create(hostname, txtUsername.text.toString(), txtPasswd.text.toString())
                    .show(fragmentManager!!, "UserRegistrationDialogFragment")
        }
    }

    fun setUpRxBinders() {
        RxTextView.textChanges(txtUsername).subscribe { text ->
            if (!TextUtils.isEmpty(text) && textInputUsername.isErrorEnabled)
                textInputUsername.setErrorEnabled(false)
        }

        RxTextView.textChanges(txtPasswd).subscribe { text ->
            if (!TextUtils.isEmpty(text) && textInputPassword.isErrorEnabled)
                textInputPassword.setErrorEnabled(false)
        }

    }

    override fun showLoader() {
        container.visibility = View.GONE
        waitingView.visibility = View.VISIBLE
    }

    override fun showErrorInUsernameEditText() {
        textInputUsername.setErrorEnabled(true);
        textInputUsername.setError("Enter a Username")
    }

    override fun showErrorInPasswordEditText() {
        textInputPassword.setErrorEnabled(true);
        textInputPassword.setError("Enter a Password")
    }

    override fun hideLoader() {
        waitingView.visibility = View.GONE
        container.visibility = View.VISIBLE
    }

    override fun showError(message: String) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun showLoginServices(loginServiceList: List<LoginServiceConfiguration>) {
        val viewMap = HashMap<String, View>()
        val supportedMap = HashMap<String, Boolean>()
        for (info in OAuthProviderInfo.LIST) {
            viewMap.put(info.serviceName, rootView.findViewById(info.buttonId))
            supportedMap.put(info.serviceName, false)
        }

        for (authProvider in loginServiceList) {
            for (info in OAuthProviderInfo.LIST) {
                if (supportedMap[info.serviceName] == false && info.serviceName == authProvider.service) {
                    supportedMap.put(info.serviceName, true)
                    viewMap[info.serviceName]?.setOnClickListener { _ ->
                        var fragment: Fragment? = null
                        try {
                            fragment = info.fragmentClass.newInstance()
                        } catch (exception: Exception) {
                            RCLog.w(exception, "failed to build new Fragment")
                        }

                        fragment?.let {
                            val args = Bundle()
                            args.putString("hostname", hostname)
                            fragment.arguments = args
                            showFragmentWithBackStack(fragment)
                        }
                    }
                    viewMap[info.serviceName]?.visibility = View.VISIBLE
                }
            }
        }

        for (info in OAuthProviderInfo.LIST) {
            if (supportedMap[info.serviceName] == false) {
                viewMap[info.serviceName]?.visibility = View.GONE
            }
        }
    }

    override fun showTwoStepAuth() {
        showFragmentWithBackStack(TwoStepAuthFragment.create(
                hostname, txtUsername.text.toString(), txtPasswd.text.toString()
        ))
    }

    override fun onResume() {
        super.onResume()
        presenter.bindView(this)
    }

    override fun onPause() {
        presenter.release()
        super.onPause()
    }
    
    override fun goBack() {
        presenter.goBack(context)
    }

}
