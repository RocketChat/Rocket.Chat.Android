package chat.rocket.android.fragment.server_config;

import android.view.View;
import android.widget.TextView;
import chat.rocket.android.R;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.MeteorLoginServiceConfiguration;
import chat.rocket.android.model.ServerConfig;
import io.realm.Realm;
import io.realm.RealmResults;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
                  .put("type", "email")
                  .put("username", username.toString())
                  .put("hashedPasswd", sha256sum(passwd.toString())))
          )
      ).continueWith(new LogcatIfError());
    });
  }

  private static String sha256sum(String orig) {
    MessageDigest d = null;
    try {
      d = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
    d.update(orig.getBytes());

    StringBuilder sb = new StringBuilder();
    for(byte b : d.digest()) sb.append(String.format("%02x", b & 0xff));

    return sb.toString();
  }

  private void onRenderAuthProviders(List<MeteorLoginServiceConfiguration> authProviders) {
    final View btnTwitter = rootView.findViewById(R.id.btn_login_with_twitter);
    final View btnGitHub = rootView.findViewById(R.id.btn_login_with_github);

    boolean hasTwitter = false;
    boolean hasGitHub = false;
    for (MeteorLoginServiceConfiguration authProvider : authProviders) {
      if (!hasTwitter && "twitter".equals(authProvider.getService())) {
        hasTwitter = true;
        btnTwitter.setOnClickListener(view -> {
          setAuthType("twitter");
        });
      }
      if (!hasGitHub && "github".equals(authProvider.getService())) {
        hasGitHub = true;
        btnGitHub.setOnClickListener(view -> {
          setAuthType("github");
        });
      }
    }

    btnTwitter.setVisibility(hasTwitter ? View.VISIBLE : View.GONE);
    btnGitHub.setVisibility(hasGitHub ? View.VISIBLE : View.GONE);
  }

  private void setAuthType(final String authType) {
    RealmHelperBolts.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
            .put("id", serverConfigId)
            .put("credential", new JSONObject()
                .put("type", authType)))
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
