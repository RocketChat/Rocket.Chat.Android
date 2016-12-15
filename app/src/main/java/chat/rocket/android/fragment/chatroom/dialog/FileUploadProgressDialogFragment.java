package chat.rocket.android.fragment.chatroom.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import chat.rocket.android.R;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.internal.FileUploading;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.renderer.FileUploadingRenderer;

/**
 * dialog fragment to display progress of file uploading.
 */
public class FileUploadProgressDialogFragment extends AbstractChatroomDialogFragment {

  private String uplId;
  private RealmObjectObserver<FileUploading> fileUploadingObserver;

  public FileUploadProgressDialogFragment() {}

  public static FileUploadProgressDialogFragment create(String serverConfigId,
      String roomId, String uplId) {
    Bundle args = new Bundle();
    args.putString("serverConfigId", serverConfigId);
    args.putString("roomId", roomId);
    args.putString("uplId", uplId);

    FileUploadProgressDialogFragment fragment = new FileUploadProgressDialogFragment();
    fragment.setArguments(args);

    return fragment;
  }

  @Override protected void handleArgs(@NonNull Bundle args) {
    super.handleArgs(args);
    uplId = args.getString("uplId");
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    fileUploadingObserver = realmHelper
        .createObjectObserver(realm -> realm.where(FileUploading.class).equalTo("uplId", uplId))
        .setOnUpdateListener(this::onRenderFileUploadingState);
  }

  @Override protected int getLayout() {
    return R.layout.dialog_file_uploading;
  }

  @Override protected void onSetupDialog() {

  }

  private void onRenderFileUploadingState(FileUploading state) {
    if (state == null) {
      return;
    }

    int syncstate = state.getSyncstate();
    if (syncstate == SyncState.SYNCED) {
      dismiss();
    } else if (syncstate == SyncState.FAILED) {
      Toast.makeText(getContext(), state.getError(), Toast.LENGTH_SHORT).show();
      //TODO: prompt retry.
      dismiss();
    } else {
      new FileUploadingRenderer(getContext(), state)
          .progressInto((ProgressBar) getDialog().findViewById(R.id.progressBar))
          .progressTextInto(
              (TextView) getDialog().findViewById(R.id.txt_filesize_uploaded),
              (TextView) getDialog().findViewById(R.id.txt_filesize_total));
    }
  }

  @Override public void onResume() {
    super.onResume();
    fileUploadingObserver.sub();
  }

  @Override public void onPause() {
    fileUploadingObserver.unsub();
    super.onPause();
  }

  @Override public void onCancel(DialogInterface dialog) {
    //TODO: should cancel uploading? or continue with showing notification with progress?
  }
}
