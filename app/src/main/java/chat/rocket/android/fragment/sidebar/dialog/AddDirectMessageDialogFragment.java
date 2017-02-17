package chat.rocket.android.fragment.sidebar.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import com.jakewharton.rxbinding.widget.RxTextView;
import io.realm.Case;

import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.sidebar.dialog.SuggestUserAdapter;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.RealmAutoCompleteAdapter;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

/**
 * add Direct RealmMessage.
 */
public class AddDirectMessageDialogFragment extends AbstractAddRoomDialogFragment {
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

  @Override
  protected void onSetupDialog() {
    View buttonAddDirectMessage = getDialog().findViewById(R.id.btn_add_direct_message);
    AutoCompleteTextView autoCompleteTextView =
        (AutoCompleteTextView) getDialog().findViewById(R.id.editor_username);

    RealmAutoCompleteAdapter<RealmUser> adapter =
        realmHelper.createAutoCompleteAdapter(getContext(),
            (realm, text) -> realm.where(RealmUser.class)
                .contains(RealmUser.USERNAME, text, Case.INSENSITIVE)
                .findAllSorted(RealmUser.USERNAME),
            context -> new SuggestUserAdapter(context, hostname));
    autoCompleteTextView.setAdapter(adapter);

    RxJavaInterop.toV2Flowable(RxTextView.textChanges(autoCompleteTextView))
        .map(text -> !TextUtils.isEmpty(text))
        .compose(bindToLifecycle())
        .subscribe(buttonAddDirectMessage::setEnabled);

    buttonAddDirectMessage.setOnClickListener(view -> createRoom());
  }

  @Override
  protected Task<Void> getMethodCallForSubmitAction() {
    String username =
        ((TextView) getDialog().findViewById(R.id.editor_username)).getText().toString();
    return methodCall.createDirectMessage(username);
  }
}
