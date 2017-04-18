package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;

public class TwoStepAuthFragment extends AbstractServerConfigFragment
    implements TwoStepAuthContract.View {

  private static final String ARG_USERNAME_OR_EMAIL = "usernameOrEmail";
  private static final String ARG_PASSWORD = "password";

  private View waitingView;
  private View submitButton;

  private TwoStepAuthContract.Presenter presenter;

  public static TwoStepAuthFragment create(String hostname, String usernameOrEmail,
                                           String password) {
    Bundle args = new Bundle();
    args.putString(AbstractServerConfigFragment.KEY_HOSTNAME, hostname);
    args.putString(ARG_USERNAME_OR_EMAIL, usernameOrEmail);
    args.putString(ARG_PASSWORD, password);

    TwoStepAuthFragment fragment = new TwoStepAuthFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args == null || !args.containsKey(ARG_USERNAME_OR_EMAIL) || !args
        .containsKey(ARG_PASSWORD)) {
      finish();
      return;
    }

    presenter = new TwoStepAuthPresenter(
        new MethodCallHelper(getContext(), hostname),
        args.getString(ARG_USERNAME_OR_EMAIL),
        args.getString(ARG_PASSWORD)
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
  public void showLoading() {
    submitButton.setEnabled(false);
    waitingView.setVisibility(View.VISIBLE);
  }

  @Override
  public void hideLoading() {
    waitingView.setVisibility(View.GONE);
    submitButton.setEnabled(true);
  }

  @Override
  public void showError(String message) {
    Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
  }

  @Override
  protected int getLayout() {
    return R.layout.fragment_two_step_auth;
  }

  @Override
  protected void onSetupView() {
    waitingView = rootView.findViewById(R.id.waiting);

    final TextView twoStepCodeTextView = (TextView) rootView.findViewById(R.id.two_step_code);

    submitButton = rootView.findViewById(R.id.btn_two_step_login);
    submitButton.setOnClickListener(view -> {
      presenter.onCode(twoStepCodeTextView.getText().toString());
    });
  }
}
