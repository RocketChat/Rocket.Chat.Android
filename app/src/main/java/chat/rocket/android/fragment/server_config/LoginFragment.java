package chat.rocket.android.fragment.server_config;

import android.view.View;
import chat.rocket.android.R;
import chat.rocket.android.model.MeteorLoginServiceConfiguration;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import jp.co.crowdworks.realm_java_helpers.RealmListObserver;

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

  }

  private void onRenderAuthProviders(List<MeteorLoginServiceConfiguration> authProviders) {
    final View btnTwitter = rootView.findViewById(R.id.btn_login_with_twitter);
    final View btnGitHub = rootView.findViewById(R.id.btn_login_with_github);

    boolean hasTwitter = false;
    boolean hasGitHub = false;
    for (MeteorLoginServiceConfiguration authProvider : authProviders) {
      if (!hasTwitter && "twitter".equals(authProvider.getService())) {
        hasTwitter = true;
      }
      if (!hasGitHub && "github".equals(authProvider.getService())) {
        hasGitHub = true;
      }
    }

    btnTwitter.setVisibility(hasTwitter ? View.VISIBLE : View.GONE);
    btnGitHub.setVisibility(hasGitHub ? View.VISIBLE : View.GONE);
  }
}
