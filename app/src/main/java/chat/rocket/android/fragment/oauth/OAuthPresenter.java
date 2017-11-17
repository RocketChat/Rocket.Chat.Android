package chat.rocket.android.fragment.oauth;

import org.json.JSONObject;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.shared.BasePresenter;
import chat.rocket.core.repositories.LoginServiceConfigurationRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class OAuthPresenter extends BasePresenter<OAuthContract.View>
    implements OAuthContract.Presenter {

  private final LoginServiceConfigurationRepository loginServiceConfigurationRepository;
  private final MethodCallHelper methodCallHelper;

  public OAuthPresenter(LoginServiceConfigurationRepository loginServiceConfigurationRepository,
                        MethodCallHelper methodCallHelper) {
    this.loginServiceConfigurationRepository = loginServiceConfigurationRepository;
    this.methodCallHelper = methodCallHelper;
  }

  @Override
  public void loadService(String serviceName) {
    addSubscription(
        loginServiceConfigurationRepository.getByName(serviceName)
            .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                optional -> {
                  if (optional.isPresent()) {
                    view.showService(optional.get());
                  } else {
                    view.close();
                  }
                },
                Logger::report
            )
    );
  }

  @Override
  public void login(JSONObject credentialJsonObject) {
    if (credentialJsonObject == null || !credentialJsonObject.optBoolean("setCredentialToken")) {
      view.showLoginError();
      return;
    }

    final String credentialToken = credentialJsonObject.optString("credentialToken");
    final String credentialSecret = credentialJsonObject.optString("credentialSecret");

    if (TextUtils.isEmpty(credentialToken) || TextUtils.isEmpty(credentialSecret)) {
      view.showLoginError();
      return;
    }

    view.showLoginDone();

    methodCallHelper.loginWithOAuth(credentialToken, credentialSecret)
        .continueWith(new LogIfError());
  }
}
