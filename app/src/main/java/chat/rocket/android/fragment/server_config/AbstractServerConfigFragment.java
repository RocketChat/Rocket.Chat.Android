package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import chat.rocket.android.R;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.helper.TextUtils;

abstract class AbstractServerConfigFragment extends AbstractFragment {
  protected String hostname;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args == null) {
      finish();
      return;
    }

    hostname = args.getString("hostname");
    if (TextUtils.isEmpty(hostname)) {
      finish();
      return;
    }
  }


  protected void showFragment(Fragment fragment) {
    getFragmentManager().beginTransaction()
        .add(R.id.content, fragment)
        .commit();
  }

  protected void showFragmentWithBackStack(Fragment fragment) {
    getFragmentManager().beginTransaction()
        .add(R.id.content, fragment)
        .addToBackStack(null)
        .commit();
  }
}
