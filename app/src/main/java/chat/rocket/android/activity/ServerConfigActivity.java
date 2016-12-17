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
import chat.rocket.android.service.RocketChatService;

/**
 * Activity for Login, Sign-up, and Connecting...
 */
public class ServerConfigActivity extends AbstractFragmentActivity {

  private String serverConfigId;
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

    serverConfigId = intent.getStringExtra("serverConfigId");
    if (TextUtils.isEmpty(serverConfigId)) {
      finish();
      return;
    }

    sessionObserver = RealmStore.get(serverConfigId)
        .createObjectObserver(Session::queryDefaultSession)
        .setOnUpdateListener(this::onRenderServerConfigSession);

    setContentView(R.layout.simple_screen);
    showFragment(new WaitingFragment());
  }

  @Override
  protected void onResume() {
    super.onResume();
    RocketChatService.keepalive(this);
    sessionObserver.sub();
  }

  @Override
  protected void onPause() {
    sessionObserver.unsub();
    super.onPause();
  }

  private void onRenderServerConfigSession(Session session) {
    if (session == null) {
      showFragment(new LoginFragment());
      return;
    }

    if (session.isTokenVerified() && TextUtils.isEmpty(session.getError())) {
      finish();
      overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
      return;
    }

    final String token = session.getToken();
    if (!TextUtils.isEmpty(token)) {
      if (TextUtils.isEmpty(session.getError())) {
        showFragment(
            WaitingFragment.create(getString(R.string.server_config_activity_authenticating)));
      } else {
        showFragment(new RetryLoginFragment());
      }
      return;
    }

    showFragment(new LoginFragment());
  }

  @Override
  protected void showFragment(Fragment fragment) {
    injectServerConfigIdArgTo(fragment);
    super.showFragment(fragment);
  }

  @Override
  protected void showFragmentWithBackStack(Fragment fragment) {
    injectServerConfigIdArgTo(fragment);
    super.showFragmentWithBackStack(fragment);
  }

  private void injectServerConfigIdArgTo(Fragment fragment) {
    Bundle args = fragment.getArguments();
    if (args == null) {
      args = new Bundle();
    }
    args.putString("serverConfigId", serverConfigId);
    fragment.setArguments(args);
  }

  @Override
  protected void onBackPressedNotHandled() {
    moveTaskToBack(true);
  }
}
