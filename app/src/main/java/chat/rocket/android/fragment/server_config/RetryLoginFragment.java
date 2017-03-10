package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;

/**
 * Login screen.
 */
public class RetryLoginFragment extends AbstractServerConfigFragment
    implements RetryLoginContract.View {

  private RetryLoginContract.Presenter presenter;

  private View btnRetry;
  private View waitingView;
  private TextView txtError;

  @Override
  protected int getLayout() {
    return R.layout.fragment_retry_login;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    presenter = new RetryLoginPresenter(
        new SessionInteractor(new RealmSessionRepository(hostname)),
        new MethodCallHelper(getContext(), hostname)
    );
  }

  @Override
  public void onResume() {
    super.onResume();
    presenter.bindView(this);
  }

  @Override
  public void onPause() {
    presenter.release();
    super.onPause();
  }

  @Override
  protected void onSetupView() {
    btnRetry = rootView.findViewById(R.id.btn_retry_login);
    waitingView = rootView.findViewById(R.id.waiting);
    txtError = (TextView) rootView.findViewById(R.id.txt_error_description);
  }

  @Override
  public void showRetry(String token) {
    waitingView.setVisibility(View.GONE);
    btnRetry.setOnClickListener(view -> presenter.onLogin(token));
  }

  @Override
  public void showError(String message) {
    txtError.setText(message);
  }

  @Override
  public void showLoader() {
    btnRetry.setEnabled(false);
    waitingView.setVisibility(View.VISIBLE);
  }

  @Override
  public void hideLoader() {
    btnRetry.setEnabled(true);
    waitingView.setVisibility(View.GONE);
  }
}
