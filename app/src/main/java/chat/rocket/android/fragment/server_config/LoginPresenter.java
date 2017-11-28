package chat.rocket.android.fragment.server_config;

import android.support.annotation.NonNull;

import com.hadisatrio.optional.Optional;

import bolts.Task;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.api.TwoStepAuthException;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.PublicSettingsConstants;
import chat.rocket.core.models.PublicSetting;
import chat.rocket.core.repositories.LoginServiceConfigurationRepository;
import chat.rocket.core.repositories.PublicSettingRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class LoginPresenter extends BasePresenter<LoginContract.View>
    implements LoginContract.Presenter {

  private final LoginServiceConfigurationRepository loginServiceConfigurationRepository;
  private final PublicSettingRepository publicSettingRepository;
  private final MethodCallHelper methodCallHelper;

  public LoginPresenter(LoginServiceConfigurationRepository loginServiceConfigurationRepository,
                        PublicSettingRepository publicSettingRepository,
                        MethodCallHelper methodCallHelper) {
    this.loginServiceConfigurationRepository = loginServiceConfigurationRepository;
    this.publicSettingRepository = publicSettingRepository;
    this.methodCallHelper = methodCallHelper;
  }

  @Override
  public void bindView(@NonNull LoginContract.View view) {
    super.bindView(view);

    getLoginServices();
  }

  @Override
  public void login(String username, String password) {
    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
      return;
    }

    view.showLoader();

    addSubscription(
        publicSettingRepository.getById(PublicSettingsConstants.LDAP.ENABLE)
            .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                publicSettingOptional -> doLogin(username, password, publicSettingOptional),
                Logger::report
            )
    );
  }

  private void getLoginServices() {
    addSubscription(
        loginServiceConfigurationRepository.getAll()
            .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                loginServiceConfigurations -> view.showLoginServices(loginServiceConfigurations),
                Logger::report
            )
    );
  }

  private void doLogin(String username, String password, Optional<PublicSetting> optional) {
    call(username, password, optional)
        .continueWith(task -> {
          if (task.isFaulted()) {
            view.hideLoader();

            final Exception error = task.getError();

            if (error instanceof TwoStepAuthException) {
              view.showTwoStepAuth();
            } else {
              view.showError(error.getMessage());
            }
          }
          return null;
        }, Task.UI_THREAD_EXECUTOR);
  }

  private Task<Void> call(String username, String password, Optional<PublicSetting> optional) {
    if (optional.isPresent() && optional.get().getValueAsBoolean()) {
      return methodCallHelper.loginWithLdap(username, password);
    }

    return methodCallHelper.loginWithEmail(username, password);
  }
}
