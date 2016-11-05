package chat.rocket.android.fragment.server_config;

import chat.rocket.android.R;

/**
 * Just showing "connecting..." screen.
 */
public class ConnectingToHostFragment extends AbstractServerConfigFragment {
  @Override protected int getLayout() {
    return R.layout.fragment_wait_for_connection;
  }

  @Override protected void onSetupView() {

  }
}
