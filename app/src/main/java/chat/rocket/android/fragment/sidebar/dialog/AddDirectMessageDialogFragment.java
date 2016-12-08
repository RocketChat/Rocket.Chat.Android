package chat.rocket.android.fragment.sidebar.dialog;

import android.view.View;
import android.widget.AutoCompleteTextView;
import chat.rocket.android.R;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.sidebar.dialog.SuggestUserAdapter;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.realm_helper.RealmAutoCompleteAdapter;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import io.realm.Case;

/**
 * add Direct Message.
 */
public class AddDirectMessageDialogFragment extends AbstractAddRoomDialogFragment {
  public AddDirectMessageDialogFragment() {}

  @Override protected int getLayout() {
    return R.layout.dialog_add_direct_message;
  }

  @Override protected void onSetupDialog() {
    View buttonAddDirectMessage = getDialog().findViewById(R.id.btn_add_direct_message);
    AutoCompleteTextView autoCompleteTextView =
        (AutoCompleteTextView) getDialog().findViewById(R.id.editor_username);

    RealmAutoCompleteAdapter<User> adapter = realmHelper.createAutoCompleteAdapter(getContext(),
        (realm, text) -> realm.where(User.class)
            .contains("username", text, Case.INSENSITIVE)
            .findAllSorted("username"),
        context -> new SuggestUserAdapter(context, hostname));
    autoCompleteTextView.setAdapter(adapter);

    RxTextView.textChanges(autoCompleteTextView)
        .map(text -> !TextUtils.isEmpty(text))
        .compose(bindToLifecycle())
        .subscribe(RxView.enabled(buttonAddDirectMessage));

    //buttonAddDirectMessage.setOnClick...
  }
}
