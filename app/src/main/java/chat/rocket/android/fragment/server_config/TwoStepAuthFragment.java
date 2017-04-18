package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import chat.rocket.android.R;

public class TwoStepAuthFragment extends AbstractServerConfigFragment
    implements TwoStepAuthContract.View {

  private View waitingView;

  private TwoStepAuthContract.Presenter presenter;

  public static TwoStepAuthFragment create(String hostname) {
    Bundle args = new Bundle();
    args.putString(AbstractServerConfigFragment.KEY_HOSTNAME, hostname);

    TwoStepAuthFragment fragment = new TwoStepAuthFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void showLoading() {
  }

  @Override
  public void hideLoading() {
  }

  @Override
  public void showError(String message) {
  }

  @Override
  protected int getLayout() {
    return R.layout.fragment_two_step_auth;
  }

  @Override
  protected void onSetupView() {
    waitingView = rootView.findViewById(R.id.waiting);

    final TextView twoStepCodeTextView = (TextView) rootView.findViewById(R.id.two_step_code);

    final View submit = rootView.findViewById(R.id.btn_two_step_login);
    submit.setOnClickListener(view -> {});
  }
}
