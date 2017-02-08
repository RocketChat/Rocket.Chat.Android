package chat.rocket.android.fragment.chatroom.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;

import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;

abstract class AbstractChatRoomDialogFragment extends BottomSheetDialogFragment {

  protected RealmHelper realmHelper;
  protected String roomId;

  protected
  @LayoutRes
  abstract int getLayout();

  protected abstract void onSetupDialog();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    if (args != null) {
      handleArgs(args);
    }
  }

  protected void handleArgs(@NonNull Bundle args) {
    String hostname = args.getString("hostname");
    realmHelper = RealmStore.get(hostname);
    roomId = args.getString("roomId");
  }

  @Override
  public final void setupDialog(Dialog dialog, int style) {
    super.setupDialog(dialog, style);
    dialog.setContentView(getLayout());
    onSetupDialog();
  }
}
