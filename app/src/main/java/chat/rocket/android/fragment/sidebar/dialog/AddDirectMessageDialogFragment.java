package chat.rocket.android.fragment.sidebar.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.hadisatrio.optional.Optional;
import com.jakewharton.rxbinding2.widget.RxTextView;

import bolts.Task;
import chat.rocket.android.BackgroundLooper;
import chat.rocket.android.R;
import chat.rocket.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.rocket.android.helper.AbsoluteUrlHelper;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.sidebar.dialog.SuggestUserAdapter;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.persistence.realm.RealmAutoCompleteAdapter;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.repositories.RealmServerInfoRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Case;

/**
 * add Direct RealmMessage.
 */
public class AddDirectMessageDialogFragment extends AbstractAddRoomDialogFragment {

  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  public static AddDirectMessageDialogFragment create(String hostname) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);

    AddDirectMessageDialogFragment fragment = new AddDirectMessageDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected int getLayout() {
    return R.layout.dialog_add_direct_message;
  }

  @SuppressLint("RxLeakedSubscription")
  @Override
  protected void onSetupDialog() {
    View buttonAddDirectMessage = getDialog().findViewById(R.id.btn_add_direct_message);
    AutoCompleteTextView autoCompleteTextView =
        (AutoCompleteTextView) getDialog().findViewById(R.id.editor_username);

    AbsoluteUrlHelper absoluteUrlHelper = new AbsoluteUrlHelper(
        hostname,
        new RealmServerInfoRepository(),
        new RealmUserRepository(hostname),
        new SessionInteractor(new RealmSessionRepository(hostname))
    );

    compositeDisposable.add(
        absoluteUrlHelper.getRocketChatAbsoluteUrl()
            .subscribeOn(AndroidSchedulers.from(BackgroundLooper.get()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::setupView,
                Logger::report
            )
    );

    RxTextView.textChanges(autoCompleteTextView)
        .map(text -> !TextUtils.isEmpty(text))
        .compose(bindToLifecycle())
        .subscribe(
            buttonAddDirectMessage::setEnabled,
            Logger::report
        );

    buttonAddDirectMessage.setOnClickListener(view -> createRoom());
    requestFocus(autoCompleteTextView);
  }

  private void setupView(Optional<RocketChatAbsoluteUrl> rocketChatAbsoluteUrlOptional) {
    compositeDisposable.clear();

    if (!rocketChatAbsoluteUrlOptional.isPresent()) {
      return;
    }

    AutoCompleteTextView autoCompleteTextView =
        (AutoCompleteTextView) getDialog().findViewById(R.id.editor_username);

    RealmAutoCompleteAdapter<RealmUser> adapter =
        realmHelper.createAutoCompleteAdapter(getContext(),
            (realm, text) -> realm.where(RealmUser.class)
                .contains(RealmUser.USERNAME, text, Case.INSENSITIVE)
                .findAllSorted(RealmUser.USERNAME),
            context -> new SuggestUserAdapter(context, rocketChatAbsoluteUrlOptional.get(), hostname));
    autoCompleteTextView.setAdapter(adapter);
  }

  @Override
  protected Task<Void> getMethodCallForSubmitAction() {
    String username =
        ((TextView) getDialog().findViewById(R.id.editor_username)).getText().toString();
    return methodCall.createDirectMessage(username)
        .onSuccess(task -> null);
  }
}
