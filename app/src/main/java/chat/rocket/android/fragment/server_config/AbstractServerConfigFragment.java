package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import chat.rocket.android.R;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.helper.OnBackPressListener;
import chat.rocket.android.helper.TextUtils;

abstract class AbstractServerConfigFragment extends AbstractFragment
  implements OnBackPressListener {
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

  @Override public boolean onBackPressed() {
    if (getFragmentManager().getBackStackEntryCount() > 0) {
      getFragmentManager().popBackStack();
      return true;
    }

    return false;
  }
}
