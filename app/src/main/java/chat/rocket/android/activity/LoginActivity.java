package chat.rocket.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import chat.rocket.android.R;
import chat.rocket.android.fragment.server_config.LoginFragment;
import chat.rocket.android.fragment.server_config.RetryLoginFragment;
import chat.rocket.android.fragment.server_config.WaitingFragment;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.service.RocketChatService;

/**
 * Activity for Login, Sign-up, and Retry connecting...
 */
public class LoginActivity extends AbstractFragmentActivity {

  private String hostname;
  private RealmObjectObserver<Session> sessionObserver;

  @Override
  protected int getLayoutContainerForFragment() {
    return R.id.content;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    if (intent == null || intent.getExtras() == null) {
      finish();
      return;
    }

    hostname = intent.getStringExtra("hostname");
    if (TextUtils.isEmpty(hostname)) {
      finish();
      return;
    }

    sessionObserver = RealmStore.get(hostname)
        .createObjectObserver(Session::queryDefaultSession)
        .setOnUpdateListener(this::onRenderServerConfigSession);

    setContentView(R.layout.simple_screen);
    showFragment(new WaitingFragment());
  }

  @Override
  protected void onResume() {
    super.onResume();
    ConnectivityManager.getInstance(getApplicationContext()).keepAliveServer();
    sessionObserver.sub();
  }

  @Override
  protected void onDestroy() {
    sessionObserver.unsub();
    super.onDestroy();
  }

  private void onRenderServerConfigSession(Session session) {
    if (session == null) {
      showFragment(new LoginFragment());
      return;
    }

    final String token = session.getToken();
    if (!TextUtils.isEmpty(token)) {
      if (TextUtils.isEmpty(session.getError())) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
      } else {
        showFragment(new RetryLoginFragment());
      }
      return;
    }

    showFragment(new LoginFragment());
  }

  @Override
  protected void showFragment(Fragment fragment) {
    injectHostnameArgTo(fragment);
    super.showFragment(fragment);
  }

  @Override
  protected void showFragmentWithBackStack(Fragment fragment) {
    injectHostnameArgTo(fragment);
    super.showFragmentWithBackStack(fragment);
  }

  private void injectHostnameArgTo(Fragment fragment) {
    Bundle args = fragment.getArguments();
    if (args == null) {
      args = new Bundle();
    }
    args.putString("hostname", hostname);
    fragment.setArguments(args);
  }

  @Override
  protected void onBackPressedNotHandled() {
    moveTaskToBack(true);
  }
}
