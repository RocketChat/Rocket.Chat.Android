package chat.rocket.android.fragment.server_config;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;
import chat.rocket.android.R;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import io.realm.Realm;
import io.realm.RealmQuery;
import jp.co.crowdworks.realm_java_helpers.RealmObjectObserver;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONObject;

/**
 * Input server host.
 */
public class InputHostnameFragment extends AbstractServerConfigFragment {
  private Handler errorShowingHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      Toast.makeText(rootView.getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
    }
  };
  RealmObjectObserver<ServerConfig> serverConfigObserver = new RealmObjectObserver<ServerConfig>() {
    @Override protected RealmQuery<ServerConfig> query(Realm realm) {
      return realm.where(ServerConfig.class).equalTo("serverConfigId", serverConfigId);
    }

    @Override protected void onChange(ServerConfig config) {
      onRenderServerConfig(config);
    }
  };

  public InputHostnameFragment() {
  }

  @Override protected int getLayout() {
    return R.layout.fragment_input_hostname;
  }

  @Override protected void onSetupView() {
    rootView.findViewById(R.id.btn_connect).setOnClickListener(view -> handleConnect());

    serverConfigObserver.sub();
  }

  private void handleConnect() {
    final TextView editor = (TextView) rootView.findViewById(R.id.editor_hostname);

    final String hostname =
        TextUtils.or(TextUtils.or(editor.getText(), editor.getHint()), "").toString();

    RealmHelperBolts.executeTransaction(
        realm -> realm.createOrUpdateObjectFromJson(ServerConfig.class,
            new JSONObject().put("serverConfigId", serverConfigId)
                .put("hostname", hostname)
                .put("connectionError", JSONObject.NULL)
                .put("session", JSONObject.NULL))).continueWith(new LogcatIfError());
  }

  @Override public void onResume() {
    super.onResume();
    serverConfigObserver.keepalive();
  }

  @Override public void onDestroyView() {
    serverConfigObserver.unsub();
    super.onDestroyView();
  }

  private void showError(String errString) {
    errorShowingHandler.removeMessages(0);
    Message msg = Message.obtain(errorShowingHandler, 0, errString);
    errorShowingHandler.sendMessageDelayed(msg, 160);
  }

  private void onRenderServerConfig(ServerConfig config) {
    final TextView editor = (TextView) rootView.findViewById(R.id.editor_hostname);

    if (!TextUtils.isEmpty(config.getHostname())) {
      editor.setText(config.getHostname());
    }
    if (!TextUtils.isEmpty(config.getConnectionError())) {
      clearConnectionErrorAndHostname();
      showError(config.getConnectionError());
    }
  }

  private void clearConnectionErrorAndHostname() {
    RealmHelperBolts.executeTransaction(
        realm -> realm.createOrUpdateObjectFromJson(ServerConfig.class,
            new JSONObject().put("serverConfigId", serverConfigId)
                .put("hostname", JSONObject.NULL)
                .put("connectionError", JSONObject.NULL))).continueWith(new LogcatIfError());
  }
}
