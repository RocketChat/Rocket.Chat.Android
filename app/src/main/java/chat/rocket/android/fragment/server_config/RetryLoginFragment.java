package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import chat.rocket.persistence.realm.RealmObjectObserver;
import chat.rocket.persistence.realm.RealmStore;

/**
 * Login screen.
 */
public class RetryLoginFragment extends AbstractServerConfigFragment {
  private RealmObjectObserver<RealmSession> sessionObserver;

  @Override
  protected int getLayout() {
    return R.layout.fragment_retry_login;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sessionObserver = RealmStore.get(hostname)
        .createObjectObserver(RealmSession::queryDefaultSession)
        .setOnUpdateListener(this::onRenderServerConfigSession);
  }

  @Override
  protected void onSetupView() {
  }

  private void onRenderServerConfigSession(RealmSession session) {
    if (session == null) {
      return;
    }

    final String token = session.getToken();
    if (!TextUtils.isEmpty(token)) {
      final View btnRetry = rootView.findViewById(R.id.btn_retry_login);
      final View waitingView = rootView.findViewById(R.id.waiting);
      waitingView.setVisibility(View.GONE);
      btnRetry.setOnClickListener(view -> {
        view.setEnabled(false);
        waitingView.setVisibility(View.VISIBLE);

        new MethodCallHelper(getContext(), hostname).loginWithToken(token)
            .continueWith(task -> {
              if (task.isFaulted()) {
                view.setEnabled(true);
                waitingView.setVisibility(View.GONE);
              }
              return null;
            });
      });
    }

    final String error = session.getError();
    final TextView txtError = (TextView) rootView.findViewById(R.id.txt_error_description);
    if (!TextUtils.isEmpty(error)) {
      txtError.setText(error);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    sessionObserver.sub();
  }

  @Override
  public void onPause() {
    sessionObserver.unsub();
    super.onPause();
  }
}
