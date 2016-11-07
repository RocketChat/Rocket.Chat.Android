package chat.rocket.android.fragment.server_config;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import chat.rocket.android.R;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.MeteorLoginServiceConfiguration;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ServerConfigCredential;
import chat.rocket.android.renderer.ServerConfigCredentialRenderer;
import io.realm.Realm;
import io.realm.RealmResults;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import jp.co.crowdworks.realm_java_helpers.RealmHelper;
import jp.co.crowdworks.realm_java_helpers.RealmListObserver;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONObject;

/**
 * Login screen.
 */
public class LoginFragment extends AbstractServerConfigFragment {
  @Override protected int getLayout() {
    return R.layout.fragment_login;
  }

  private Handler errorShowingHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      Toast.makeText(rootView.getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
    }
  };

  private RealmListObserver<MeteorLoginServiceConfiguration> authProvidersObserver =
      new RealmListObserver<MeteorLoginServiceConfiguration>() {
        @Override protected RealmResults<MeteorLoginServiceConfiguration> queryItems(Realm realm) {
          return realm.where(MeteorLoginServiceConfiguration.class)
              .equalTo("serverConfig.id", serverConfigId)
              .findAll();
        }

        @Override protected void onCollectionChanged(List<MeteorLoginServiceConfiguration> list) {
          onRenderAuthProviders(list);
        }
      };

  @Override protected void onSetupView() {
    final View btnEmail = rootView.findViewById(R.id.btn_login_with_email);
    final TextView txtUsername = (TextView) rootView.findViewById(R.id.editor_username);
    final TextView txtPasswd = (TextView) rootView.findViewById(R.id.editor_passwd);
    btnEmail.setOnClickListener(view -> {
      final CharSequence username = txtUsername.getText();
      final CharSequence passwd = txtPasswd.getText();
      if (TextUtils.isEmpty(username) || TextUtils.isEmpty(passwd)) return;

      RealmHelperBolts.executeTransaction(realm ->
          realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
              .put("id", serverConfigId)
              .put("credential", new JSONObject()
                  .put("id", serverConfigId)
                  .put("type", ServerConfigCredential.TYPE_EMAIL)
                  .put("errorMessage", JSONObject.NULL)
                  .put("username", username.toString())
                  .put("hashedPasswd", sha256sum(passwd.toString())))
          )
      ).continueWith(new LogcatIfError());
    });

    showErrorIfNeeded();
  }

  private void showErrorIfNeeded() {
    ServerConfig config = RealmHelper.executeTransactionForRead(realm -> realm.where(ServerConfig.class)
        .equalTo("id", serverConfigId)
        .isNotNull("credential.errorMessage")
        .findFirst());

    if (config != null) {
      ServerConfigCredential credential = config.getCredential();
      new ServerConfigCredentialRenderer(getContext(), credential)
          .usernameInto((TextView) rootView.findViewById(R.id.editor_username));

      String errorMessage = credential.getErrorMessage();
      if (!TextUtils.isEmpty(errorMessage)) {
        showError(errorMessage);
      }
    }
  }

  private void showError(String errString) {
    errorShowingHandler.removeMessages(0);
    Message msg = Message.obtain(errorShowingHandler, 0, errString);
    errorShowingHandler.sendMessageDelayed(msg, 160);
  }

  private static String sha256sum(String orig) {
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException exception) {
      return null;
    }
    messageDigest.update(orig.getBytes());

    StringBuilder sb = new StringBuilder();
    for (byte b : messageDigest.digest()) {
      sb.append(String.format("%02x", b & 0xff));
    }

    return sb.toString();
  }

  private void onRenderAuthProviders(List<MeteorLoginServiceConfiguration> authProviders) {
    final View btnTwitter = rootView.findViewById(R.id.btn_login_with_twitter);
    final View btnGitHub = rootView.findViewById(R.id.btn_login_with_github);

    boolean hasTwitter = false;
    boolean hasGitHub = false;
    for (MeteorLoginServiceConfiguration authProvider : authProviders) {
      if (!hasTwitter
          && ServerConfigCredential.TYPE_TWITTER.equals(authProvider.getService())) {
        hasTwitter = true;
        btnTwitter.setOnClickListener(view -> {
          setAuthType(authProvider.getId(), ServerConfigCredential.TYPE_TWITTER);
        });
      }
      if (!hasGitHub
          && ServerConfigCredential.TYPE_GITHUB.equals(authProvider.getService())) {
        hasGitHub = true;
        btnGitHub.setOnClickListener(view -> {
          setAuthType(authProvider.getId(), ServerConfigCredential.TYPE_GITHUB);
        });
      }
    }

    btnTwitter.setVisibility(hasTwitter ? View.VISIBLE : View.GONE);
    btnGitHub.setVisibility(hasGitHub ? View.VISIBLE : View.GONE);
  }

  private void setAuthType(final String authProviderId, final String authType) {
    RealmHelperBolts.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
            .put("id", serverConfigId)
            .put("credential", new JSONObject()
                .put("id", authProviderId)
                .put("type", authType))
                .put("errorMessage", JSONObject.NULL))
    ).continueWith(new LogcatIfError());
  }

  @Override public void onResume() {
    super.onResume();
    authProvidersObserver.sub();
  }

  @Override public void onPause() {
    authProvidersObserver.unsub();
    super.onPause();
  }
}
