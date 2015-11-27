package chat.rocket.android.fragment;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.api.Auth;
import chat.rocket.android.api.RocketChatAPI;
import chat.rocket.android.api.RocketChatRestAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.ServerConfig;

public class AuthenticateFragment extends AbstractFragment {
    public AuthenticateFragment(){}

    private ConstrainedActionManager mSaveAuthManager = new ConstrainedActionManager() {
        @Override
        protected void action() {
            if(mServerConfig != null) {
                RocketChatDatabaseHelper.write(getContext(), new RocketChatDatabaseHelper.DBCallback<Object>() {
                    @Override
                    public Object process(SQLiteDatabase db) {
                        mServerConfig.put(db);
                        return null;
                    }
                });
                showSplashFragment();
            }
        }
    };
    private ServerConfig mServerConfig;

    public static Fragment create(final String host, final String account, final String password) {
        Bundle args = new Bundle();
        args.putString("host", host);
        args.putString("account", account);
        args.putString("password", password);
        Fragment f = new AuthenticateFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(!isValidArgs(args)){
            finish();
            return;
        }

        handleLogin(args.getString("host"),
                args.getString("account"),
                args.getString("password"));
    }

    private void handleLogin(final String host, final String account, final String password){
        new RocketChatRestAPI(host).login(account, password)
                .onSuccess(new Continuation<Auth, Object>() {
                    @Override
                    public Object then(Task<Auth> task) throws Exception {
                        Auth auth = task.getResult();

                        mServerConfig = new ServerConfig();
                        mServerConfig.hostname = host;
                        mServerConfig.account = account;
                        mServerConfig.authUserId = auth.userId;
                        mServerConfig.authToken = auth.authToken;
                        mServerConfig.isPrimary = true;
                        mServerConfig.id = host;

                        mSaveAuthManager.setShouldAction(true);
                        return null;
                    }
                })
                .continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        if (task.isFaulted()) {
                            showErrorFragment(task.getError().getMessage());
                            finish();
                        }
                        return null;
                    }
                });

        //just for trial!!
        new RocketChatAPI(host).trial(account, password);

    }

    private void showSplashFragment(){
        getFragmentManager().beginTransaction()
                .remove(this)
                .replace(R.id.simple_framelayout, new SplashFragment())
                .commit();
    }

    private void showErrorFragment(String msg) {
        getFragmentManager().beginTransaction()
                .remove(this)
                .replace(R.id.simple_framelayout, LoginErrorFragment.create(msg))
                .commit();
    }

    private boolean isValidArgs(Bundle args) {
        if(args == null) return false;

        return (args.containsKey("host") &&
                args.containsKey("account") &&
                args.containsKey("password"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.authenticating_screen, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();
        mSaveAuthManager.setConstrainedMet(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSaveAuthManager.setConstrainedMet(false);
    }
}
