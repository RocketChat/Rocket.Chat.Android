package chat.rocket.android.fragment.server_config

import com.hadisatrio.optional.Optional

import bolts.Task
import chat.rocket.android.BackgroundLooper
import chat.rocket.android.api.MethodCallHelper
import chat.rocket.android.api.TwoStepAuthException
import chat.rocket.android.helper.Logger
import chat.rocket.android.helper.TextUtils
import chat.rocket.android.shared.BasePresenter
import chat.rocket.core.PublicSettingsConstants
import chat.rocket.core.models.PublicSetting
import chat.rocket.core.repositories.LoginServiceConfigurationRepository
import chat.rocket.core.repositories.PublicSettingRepository
import io.reactivex.android.schedulers.AndroidSchedulers

class LoginPresenter(private val loginServiceConfigurationRepository: LoginServiceConfigurationRepository,
                     private val publicSettingRepository: PublicSettingRepository,
                     private val methodCallHelper: MethodCallHelper) : BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    override fun bindView(view: LoginContract.View) {
        super.bindView(view)

        getLoginServices()
    }

    override fun login(username: String, password: String) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            return
        }

        view.showLoader()

        addSubscription(
                publicSettingRepository.getById(PublicSettingsConstants.LDAP.ENABLE)
                        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { publicSettingOptional -> doLogin(username, password, publicSettingOptional) },
                                Consumer<Throwable> { Logger.report(it) }
                        )
        )
    }

    private fun getLoginServices() {
        addSubscription(
                loginServiceConfigurationRepository.all
                        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { loginServiceConfigurations -> view.showLoginServices(loginServiceConfigurations) },
                                Consumer<Throwable> { Logger.report(it) }
                        )
        )
    }

    private fun doLogin(username: String, password: String, optional: Optional<PublicSetting>) {
        call(username, password, optional)
                .continueWith<Any>({ task ->
                    if (task.isFaulted()) {
                        view.hideLoader()

                        val error = task.getError()

                        if (error is TwoStepAuthException) {
                            view.showTwoStepAuth()
                        } else {
                            view.showError(error.message)
                        }
                    }
                    null
                }, Task.UI_THREAD_EXECUTOR)
    }

    private fun call(username: String, password: String, optional: Optional<PublicSetting>): Task<Void> {
        return if (optional.isPresent && optional.get().valueAsBoolean) {
            methodCallHelper.loginWithLdap(username, password)
        } else methodCallHelper.loginWithEmail(username, password)

    }
}
