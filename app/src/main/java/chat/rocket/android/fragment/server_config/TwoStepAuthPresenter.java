package chat.rocket.android.fragment.server_config;

import chat.rocket.android.shared.BasePresenter;

public class TwoStepAuthPresenter extends BasePresenter<TwoStepAuthContract.View>
    implements TwoStepAuthContract.Presenter {

  @Override
  public void onCode(String twoStepAuthCode) {
  }
}
