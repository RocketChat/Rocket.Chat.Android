package chat.rocket.android.fragment.chatroom.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.TextView;

import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.Logger;
import chat.rocket.core.interactors.EditMessageInteractor;
import chat.rocket.core.interactors.PermissionInteractor;
import chat.rocket.core.models.Message;
import chat.rocket.core.repositories.MessageRepository;
import chat.rocket.core.repositories.PermissionRepository;
import chat.rocket.core.repositories.PublicSettingRepository;
import chat.rocket.core.repositories.RoomRepository;
import chat.rocket.core.repositories.RoomRoleRepository;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.persistence.realm.repositories.RealmMessageRepository;
import chat.rocket.persistence.realm.repositories.RealmPermissionRepository;
import chat.rocket.persistence.realm.repositories.RealmPublicSettingRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRoleRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class MessageOptionsDialogFragment extends BottomSheetDialogFragment {

  public final static String ARG_MESSAGE_ID = "messageId";

  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private OnMessageOptionSelectedListener internalListener = new OnMessageOptionSelectedListener() {
    @Override
    public void onEdit(Message message) {
      if (externalListener != null) {
        externalListener.onEdit(message);
      }
    }
  };

  private OnMessageOptionSelectedListener externalListener = null;

  public static MessageOptionsDialogFragment create(@NonNull Message message) {
    Bundle bundle = new Bundle();
    bundle.putString(ARG_MESSAGE_ID, message.getId());

    MessageOptionsDialogFragment messageOptionsDialogFragment = new MessageOptionsDialogFragment();
    messageOptionsDialogFragment.setArguments(bundle);

    return messageOptionsDialogFragment;
  }

  public void setOnMessageOptionSelectedListener(
      OnMessageOptionSelectedListener onMessageOptionSelectedListener) {
    externalListener = onMessageOptionSelectedListener;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

    bottomSheetDialog.setContentView(R.layout.dialog_message_options);

    TextView info = (TextView) bottomSheetDialog.findViewById(R.id.message_options_info);

    Bundle args = getArguments();
    if (args == null || !args.containsKey(ARG_MESSAGE_ID)) {
      info.setText(R.string.message_options_no_message_info);
    } else {
      setUpDialog(bottomSheetDialog, args.getString(ARG_MESSAGE_ID));
    }

    return bottomSheetDialog;
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    compositeDisposable.clear();
    super.onDismiss(dialog);
  }

  private void setUpDialog(final BottomSheetDialog bottomSheetDialog, String messageId) {
    RocketChatCache cache = new RocketChatCache(bottomSheetDialog.getContext());

    String hostname = cache.getSelectedServerHostname();

    EditMessageInteractor editMessageInteractor = getEditMessageInteractor(hostname);

    MessageRepository messageRepository = new RealmMessageRepository(hostname);

    Disposable disposable = messageRepository.getById(messageId)
        .flatMap(it -> {
          if (!it.isPresent()) {
            return Single.just(Pair.<Message, Boolean>create(null, false));
          }

          Message message = it.get();

          return Single.zip(
              Single.just(message),
              editMessageInteractor.isAllowed(message),
              Pair::create
          );
        })
        .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            pair -> {
              if (pair.second) {
                bottomSheetDialog.findViewById(R.id.message_options_info)
                    .setVisibility(View.GONE);
                View editView = bottomSheetDialog.findViewById(R.id.message_options_edit_action);
                editView.setVisibility(View.VISIBLE);
                editView.setOnClickListener(view -> internalListener.onEdit(pair.first));
              } else {
                ((TextView) bottomSheetDialog.findViewById(R.id.message_options_info))
                    .setText(R.string.message_options_no_permissions_info);
              }
            },
            throwable -> {
              ((TextView) bottomSheetDialog.findViewById(R.id.message_options_info))
                  .setText(R.string.message_options_no_message_info);

              Logger.report(throwable);
            }
        );

    compositeDisposable.add(disposable);
  }

  private EditMessageInteractor getEditMessageInteractor(String hostname) {
    UserRepository userRepository = new RealmUserRepository(hostname);
    RoomRoleRepository roomRoleRepository = new RealmRoomRoleRepository(hostname);
    PermissionRepository permissionRepository = new RealmPermissionRepository(hostname);

    PermissionInteractor permissionInteractor = new PermissionInteractor(
        userRepository,
        roomRoleRepository,
        permissionRepository
    );

    MessageRepository messageRepository = new RealmMessageRepository(hostname);
    RoomRepository roomRepository = new RealmRoomRepository(hostname);
    PublicSettingRepository publicSettingRepository = new RealmPublicSettingRepository(hostname);

    return new EditMessageInteractor(
        permissionInteractor,
        userRepository,
        messageRepository,
        roomRepository,
        publicSettingRepository
    );
  }

  public interface OnMessageOptionSelectedListener {
    void onEdit(Message message);
  }
}
