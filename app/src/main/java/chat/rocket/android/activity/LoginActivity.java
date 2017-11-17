package chat.rocket.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import chat.rocket.android.R;
import chat.rocket.android.fragment.server_config.LoginFragment;
import chat.rocket.android.fragment.server_config.RetryLoginFragment;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;

/**
 * Activity for Login, Sign-up, and Retry connecting...
 */
public class LoginActivity extends AbstractFragmentActivity implements LoginContract.View {
  public static final String KEY_HOSTNAME = "hostname";

  private LoginContract.Presenter presenter;

  @Override
  protected int getLayoutContainerForFragment() {
    return R.id.content;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String hostname = null;
    Intent intent = getIntent();
    if (intent != null && intent.getExtras() != null) {
      hostname = intent.getStringExtra(KEY_HOSTNAME);
    }

    presenter = new LoginPresenter(
        hostname,
        new SessionInteractor(new RealmSessionRepository(hostname)),
        ConnectivityManager.getInstance(getApplicationContext())
    );
  }

  @Override
  protected void onResume() {
    super.onResume();
    presenter.bindView(this);
  }

  @Override
  protected void onDestroy() {
    presenter.release();
    super.onDestroy();
  }

  private void showFragment(Fragment fragment, String hostname) {
    setContentView(R.layout.simple_screen);
    injectHostnameArgTo(fragment, hostname);
    super.showFragment(fragment);
  }

  private void injectHostnameArgTo(Fragment fragment, String hostname) {
    Bundle args = fragment.getArguments();
    if (args == null) {
      args = new Bundle();
    }
    args.putString(LoginActivity.KEY_HOSTNAME, hostname);
    fragment.setArguments(args);
  }

  @Override
  protected void onBackPressedNotHandled() {
    moveTaskToBack(true);
  }

  @Override
  public void showLogin(String hostname) {
    showFragment(new LoginFragment(), hostname);
  }

  @Override
  public void showRetryLogin(String hostname) {
    showFragment(new RetryLoginFragment(), hostname);
  }

  @Override
  public void closeView() {
    finish();
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
  }
}
