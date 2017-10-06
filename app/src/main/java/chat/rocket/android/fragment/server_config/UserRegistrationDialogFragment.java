package chat.rocket.android.fragment.server_config;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.TextUtils;

/**
 * Dialog for user registration.
 */
public class UserRegistrationDialogFragment extends DialogFragment {
  private String hostname;
  private String username;
  private String email;
  private String password;

  public UserRegistrationDialogFragment() {
    super();
  }

  /**
   * build UserRegistrationDialogFragment with auto-detect email/username.
   */
  public static UserRegistrationDialogFragment create(String hostname,
                                                      String usernameOrEmail, String password) {
    if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
      return create(hostname, null, usernameOrEmail, password);
    } else {
      return create(hostname, usernameOrEmail, null, password);
    }
  }

  /**
   * build UserRegistrationDialogFragment.
   */
  public static UserRegistrationDialogFragment create(String hostname,
                                                      String username, String email,
                                                      String password) {
    Bundle args = new Bundle();
    args.putString("hostname", hostname);
    if (!TextUtils.isEmpty(username)) {
      args.putString("username", username);
    }
    if (!TextUtils.isEmpty(email)) {
      args.putString("email", email);
    }
    if (!TextUtils.isEmpty(password)) {
      args.putString("password", password);
    }
    UserRegistrationDialogFragment dialog = new UserRegistrationDialogFragment();
    dialog.setArguments(args);
    return dialog;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle args = getArguments();
    if (args != null) {
      hostname = args.getString("hostname");
      username = args.getString("username");
      email = args.getString("email");
      password = args.getString("password");
    }
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog)
        .setView(createDialogView())
        .create();
  }

  private View createDialogView() {
    View dialog = LayoutInflater.from(getContext())
        .inflate(R.layout.dialog_user_registration, null, false);

    final TextView txtUsername = (TextView) dialog.findViewById(R.id.editor_username);
    final TextView txtEmail = (TextView) dialog.findViewById(R.id.editor_email);
    final TextView txtPasswd = (TextView) dialog.findViewById(R.id.editor_passwd);

    if (!TextUtils.isEmpty(username)) {
      txtUsername.setText(username);
    }
    if (!TextUtils.isEmpty(email)) {
      txtEmail.setText(email);
    }
    if (!TextUtils.isEmpty(password)) {
      txtPasswd.setText(password);
    }

    final View waitingView = dialog.findViewById(R.id.waiting);
    waitingView.setVisibility(View.GONE);

    dialog.findViewById(R.id.btn_register_user).setOnClickListener(view -> {
      view.setEnabled(false);
      waitingView.setVisibility(View.VISIBLE);

      username = txtUsername.getText().toString();
      email = txtEmail.getText().toString();
      password = txtPasswd.getText().toString();

      MethodCallHelper methodCallHelper = new MethodCallHelper(getContext(), hostname);
      methodCallHelper.registerUser(username, email, password, password)
          .onSuccessTask(task -> methodCallHelper.loginWithEmail(email, password))
          .onSuccessTask(task -> methodCallHelper.setUsername(username)) //TODO: should prompt!
          .onSuccessTask(task -> methodCallHelper.joinDefaultChannels())
          .onSuccessTask(task -> {
            dismiss();
            return task;
          })
          .continueWith(task -> {
            if (task.isFaulted()) {
              Exception exception = task.getError();
              showError(exception.getMessage());
              view.setEnabled(true);
              waitingView.setVisibility(View.GONE);
            }
            return null;
          });
    });
    return dialog;
  }

  private void showError(String errMessage) {
    Toast.makeText(getContext(), errMessage, Toast.LENGTH_SHORT).show();
  }
}
