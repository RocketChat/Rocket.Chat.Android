package chat.rocket.android.fragment.server_config;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import chat.rocket.android.R;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import io.realm.Realm;
import io.realm.RealmQuery;
import jp.co.crowdworks.realm_java_helpers.RealmObjectObserver;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

/**
 * Input server host.
 */
public class InputHostnameFragment extends AbstractServerConfigFragment {
    public InputHostnameFragment(){}

    @Override
    protected int getLayout() {
        return R.layout.fragment_input_hostname;
    }

    RealmObjectObserver<ServerConfig> mObserver = new RealmObjectObserver<ServerConfig>() {
        @Override
        protected RealmQuery<ServerConfig> query(Realm realm) {
            return realm.where(ServerConfig.class).equalTo("id", mServerConfigId);
        }

        @Override
        protected void onChange(ServerConfig config) {
            onRenderServerConfig(config);
        }
    };

    @Override
    protected void onSetupView() {
        mRootView.findViewById(R.id.btn_connect).setOnClickListener(view -> handleConnect());

        mObserver.sub();
    }

    private void handleConnect() {
        final TextView editor = (TextView) mRootView.findViewById(R.id.editor_hostname);

        final String hostname = TextUtils.or(
                TextUtils.or(editor.getText(), editor.getHint()),
                "").toString();

        RealmHelperBolts
                .executeTransaction(realm ->
                        realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
                                .put("id", mServerConfigId)
                                .put("hostname", hostname)
                                .put("connectionError", JSONObject.NULL)))
                .continueWith(new LogcatIfError());
    }

    @Override
    public void onResume() {
        super.onResume();
        mObserver.keepalive();
    }

    @Override
    public void onDestroyView() {
        mObserver.unsub();
        super.onDestroyView();
    }

    private Handler mShowError = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(mRootView.getContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
        }
    };

    private void showError(String errString) {
        mShowError.removeMessages(0);
        Message msg = Message.obtain(mShowError, 0, errString);
        mShowError.sendMessageDelayed(msg, 160);
    }

    private void onRenderServerConfig(ServerConfig config) {
        final TextView editor = (TextView) mRootView.findViewById(R.id.editor_hostname);

        if (!TextUtils.isEmpty(config.getHostname())) editor.setText(config.getHostname());
        if (!TextUtils.isEmpty(config.getConnectionError())) {
            clearConnectionErrorAndHostname();
            showError(config.getConnectionError());
        }
    }

    private void clearConnectionErrorAndHostname() {
        RealmHelperBolts
                .executeTransaction(realm ->
                        realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
                                .put("id", mServerConfigId)
                                .put("hostname", JSONObject.NULL)
                                .put("connectionError", JSONObject.NULL)))
                .continueWith(new LogcatIfError());
    }
}
