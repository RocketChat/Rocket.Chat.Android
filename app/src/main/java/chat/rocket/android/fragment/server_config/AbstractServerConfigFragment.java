package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.helper.TextUtils;

abstract class AbstractServerConfigFragment extends AbstractFragment {
  protected String serverConfigId;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args == null) {
      finish();
      return;
    }

    serverConfigId = args.getString("id");
    if (TextUtils.isEmpty(serverConfigId)) {
      finish();
      return;
    }
  }
}
