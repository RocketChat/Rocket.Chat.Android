package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;

/**
 * Just showing "connecting..." screen.
 */
public class WaitingFragment extends AbstractServerConfigFragment {

  private String caption;

  /**
   * create new "Waiting..." screen with caption.
   */
  public static WaitingFragment create(String caption) {
    Bundle args = new Bundle();
    args.putString("caption", caption);
    WaitingFragment fragment = new WaitingFragment();
    fragment.setArguments(args);
    return fragment;
  }

  public WaitingFragment() {}

  @Override protected int getLayout() {
    return R.layout.fragment_waiting;
  }

  @Override protected void onSetupView() {
    Bundle args = getArguments();
    if (args != null) {
      caption = args.getString("caption");
    }

    TextView captionView = (TextView) rootView.findViewById(R.id.txt_caption);
    if (TextUtils.isEmpty(caption)) {
      captionView.setVisibility(View.GONE);
    } else {
      captionView.setText(caption);
      captionView.setVisibility(View.VISIBLE);
    }
  }
}
