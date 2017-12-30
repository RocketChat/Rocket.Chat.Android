package chat.rocket.android.fragment.server_config

import android.content.Context
import bolts.Continuation
import bolts.Task
import chat.rocket.android.BackgroundLooper
import chat.rocket.android.LaunchUtil
import chat.rocket.android.RocketChatApplication
import chat.rocket.android.RocketChatCache
import chat.rocket.android.api.MethodCallHelper
import chat.rocket.android.api.TwoStepAuthException
import chat.rocket.android.helper.Logger
import chat.rocket.android.helper.TextUtils
import chat.rocket.android.service.ConnectivityManager
import chat.rocket.android.shared.BasePresenter
import chat.rocket.core.PublicSettingsConstants
import chat.rocket.core.models.PublicSetting
import chat.rocket.core.repositories.LoginServiceConfigurationRepository
import chat.rocket.core.repositories.PublicSettingRepository
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

class LoginPresenter(private val loginServiceConfigurationRepository: LoginServiceConfigurationRepository,
                     private val publicSettingRepository: PublicSettingRepository,
                     private val methodCallHelper: MethodCallHelper) : BasePresenter<LoginContract.View>(), LoginContract.Presenter {

    override fun bindView(view: LoginContract.View) {
        super.bindView(view)

        getLoginServices()
    }


    override fun goBack(ctx: Context?) {
        val context = RocketChatApplication.getInstance()
        val hostname = RocketChatCache.getSelectedServerHostname()
        hostname?.let {
            ConnectivityManager.getInstance(context).removeServer(hostname)
            RocketChatCache.clearSelectedHostnameReferences()

        }
        LaunchUtil.showMainActivity(ctx)
    }

    override fun login(username: String, password: String) {

        //set error to edit texts
        if (TextUtils.isEmpty(username) && TextUtils.isEmpty(password)) {
            view.showErrorInUsernameEditText()
            view.showErrorInPasswordEditText()
            return
        }
        if (TextUtils.isEmpty(username)) {
            view.showErrorInUsernameEditText()
            return
        }
        if (TextUtils.isEmpty(password)) {
            view.showErrorInPasswordEditText()
            return
        }

        view.showLoader()

        addSubscription(
                publicSettingRepository.getById(PublicSettingsConstants.LDAP.ENABLE)
                        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onSuccess = { publicSettingOptional -> doLogin(username, password, publicSettingOptional) },
                                onError = { Logger.report(it) }
                        )
        )
    }

    private fun getLoginServices() {
        addSubscription(
                loginServiceConfigurationRepository.all
                        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onNext = { loginServiceConfigurations ->
                                    view.showLoginServices(loginServiceConfigurations);
                                },
                                onError = { Logger.report(it) }
                        )
        )
    }

    private fun doLogin(username: String, password: String, optional: Optional<PublicSetting>) {
        addSubscription(
                Completable.create {
                    call(username, password, optional)
                            .continueWith(object : Continuation<Void, Any?> {
                                override fun then(task: Task<Void>?): Any? {
                                    if (task != null && task.isFaulted()) {
                                        view.hideLoader()

                                        val error = task.getError()

                                        error?.let {
                                            if (error is TwoStepAuthException) {
                                                view.showTwoStepAuth()
                                            } else {
                                                view.showError(error.message)
                                            }
                                        }
                                        return Completable.complete()
                                    }
                                    return null
                                }
                            }, Task.UI_THREAD_EXECUTOR)
                }.subscribeBy(
                        onError = {
                            view.showError(it.message)
                        }
                )
        )
    }

    private fun call(username: String, password: String, optional: Optional<PublicSetting>): Task<Void> {
        return if (optional.isPresent && optional.get().valueAsBoolean) {
            methodCallHelper.loginWithLdap(username, password)
        } else methodCallHelper.loginWithEmail(username, password)

    }
}
