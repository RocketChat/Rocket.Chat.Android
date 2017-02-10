package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.oauth.OAuthProviderInfo;
import chat.rocket.android.log.RCLog;
import chat.rocket.persistence.realm.models.ddp.RealmMeteorLoginServiceConfiguration;
import chat.rocket.persistence.realm.RealmListObserver;
import chat.rocket.persistence.realm.RealmStore;

/**
 * Login screen.
 */
public class LoginFragment extends AbstractServerConfigFragment {
  private RealmListObserver<RealmMeteorLoginServiceConfiguration> authProvidersObserver;

  @Override
  protected int getLayout() {
    return R.layout.fragment_login;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    authProvidersObserver = RealmStore.get(hostname)
        .createListObserver(realm -> realm.where(RealmMeteorLoginServiceConfiguration.class).findAll())
        .setOnUpdateListener(this::onRenderAuthProviders);
  }

  @Override
  protected void onSetupView() {
    final View btnEmail = rootView.findViewById(R.id.btn_login_with_email);
    final TextView txtUsername = (TextView) rootView.findViewById(R.id.editor_username);
    final TextView txtPasswd = (TextView) rootView.findViewById(R.id.editor_passwd);
    final View waitingView = rootView.findViewById(R.id.waiting);
    btnEmail.setOnClickListener(view -> {
      final CharSequence username = txtUsername.getText();
      final CharSequence passwd = txtPasswd.getText();
      if (TextUtils.isEmpty(username) || TextUtils.isEmpty(passwd)) {
        return;
      }
      view.setEnabled(false);
      waitingView.setVisibility(View.VISIBLE);

      new MethodCallHelper(getContext(), hostname)
          .loginWithEmail(username.toString(), passwd.toString())
          .continueWith(task -> {
            if (task.isFaulted()) {
              showError(task.getError().getMessage());
              view.setEnabled(true);
              waitingView.setVisibility(View.GONE);
            }
            return null;
          });
    });

    final View btnUserRegistration = rootView.findViewById(R.id.btn_user_registration);
    btnUserRegistration.setOnClickListener(view -> UserRegistrationDialogFragment.create(hostname,
        txtUsername.getText().toString(), txtPasswd.getText().toString())
        .show(getFragmentManager(), UserRegistrationDialogFragment.class.getSimpleName()));
  }

  private void showError(String errString) {
    Snackbar.make(rootView, errString, Snackbar.LENGTH_SHORT).show();
  }

  private void onRenderAuthProviders(List<RealmMeteorLoginServiceConfiguration> authProviders) {
    HashMap<String, View> viewMap = new HashMap<>();
    HashMap<String, Boolean> supportedMap = new HashMap<>();
    for (OAuthProviderInfo info : OAuthProviderInfo.LIST) {
      viewMap.put(info.serviceName, rootView.findViewById(info.buttonId));
      supportedMap.put(info.serviceName, false);
    }

    for (RealmMeteorLoginServiceConfiguration authProvider : authProviders) {
      for (OAuthProviderInfo info : OAuthProviderInfo.LIST) {
        if (!supportedMap.get(info.serviceName)
            && info.serviceName.equals(authProvider.getService())) {
          supportedMap.put(info.serviceName, true);
          viewMap.get(info.serviceName).setOnClickListener(view -> {
            Fragment fragment = null;
            try {
              fragment = info.fragmentClass.newInstance();
            } catch (Exception exception) {
              RCLog.w(exception, "failed to create new Fragment");
            }
            if (fragment != null) {
              Bundle args = new Bundle();
              args.putString("hostname", hostname);
              fragment.setArguments(args);
              showFragmentWithBackStack(fragment);
            }
          });
          viewMap.get(info.serviceName).setVisibility(View.VISIBLE);
        }
      }
    }

    for (OAuthProviderInfo info : OAuthProviderInfo.LIST) {
      if (!supportedMap.get(info.serviceName)) {
        viewMap.get(info.serviceName).setVisibility(View.GONE);
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    authProvidersObserver.sub();
  }

  @Override
  public void onPause() {
    authProvidersObserver.unsub();
    super.onPause();
  }
}
