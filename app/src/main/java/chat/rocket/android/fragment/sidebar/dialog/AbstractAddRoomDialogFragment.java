package chat.rocket.android.fragment.sidebar.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;
import com.trello.rxlifecycle.components.support.RxAppCompatDialogFragment;

public abstract class AbstractAddRoomDialogFragment extends RxAppCompatDialogFragment {

  protected RealmHelper realmHelper;
  protected String hostname;

  protected @LayoutRes abstract int getLayout();

  protected abstract void onSetupDialog();

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args != null) {
      handleArgs(args);
    }
  }

  protected void handleArgs(@NonNull Bundle args) {
    String serverConfigId = args.getString("serverConfigId");
    realmHelper = RealmStore.get(serverConfigId);
    hostname = args.getString("hostname");
  }

  @Override public final void setupDialog(Dialog dialog, int style) {
    super.setupDialog(dialog, style);
    dialog.setContentView(getLayout());
    onSetupDialog();
  }
}
