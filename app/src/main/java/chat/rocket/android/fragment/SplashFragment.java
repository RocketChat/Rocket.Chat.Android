package chat.rocket.android.fragment;

import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import chat.rocket.android.R;
import chat.rocket.android.activity.MainActivity;
import chat.rocket.android.api.ws.RocketChatWSService;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;

public class SplashFragment extends AbstractFragment {

    public SplashFragment() {}

    private ConstrainedActionManager mShowMainActivityManager = new ConstrainedActionManager() {
        @Override
        protected void action() {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ServerConfig s = getPrimaryServerConfig();

        if (s == null || TextUtils.isEmpty(s.hostname)) {
            RocketChatWSService.kill(getContext());
            showServerHostConfigFragment();
            return;
        }

        RocketChatWSService.keepalive(getContext());

        if(!TextUtils.isEmpty(s.authToken) ||
                (s.authType == ServerConfig.AuthType.EMAIL && !TextUtils.isEmpty(s.password)) ||
                (s.authType == ServerConfig.AuthType.GITHUB && !TextUtils.isEmpty(s.password)) ) {
            login(s);
        }
        else{
            showServerAuthConfigFragment();
        }
    }

    private void showServerHostConfigFragment(){
        getFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout, new ServerHostConfigFragment())
                .commit();
    }

    private void showServerAuthConfigFragment(){
        getFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout, new ServerAuthConfigFragment())
                .commit();
    }

    private void login(ServerConfig s){
        if(s.syncstate==SyncState.SYNCED) {
            //force login!
            s.syncstate = SyncState.NOT_SYNCED;
            s.putByContentProvider(getContext());
        }

        getContext().getContentResolver().registerContentObserver(RocketChatProvider.getUriForQuery(ServerConfig.TABLE_NAME, s._id), false, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                if(getContext()==null) return;
                ServerConfig s = getPrimaryServerConfig();
                if(s==null) { //deleted.
                    getContext().getContentResolver().unregisterContentObserver(this);
                    RocketChatWSService.kill(getContext());
                    showServerHostConfigFragment();
                } else if(s.syncstate==SyncState.SYNCED) {
                    getContext().getContentResolver().unregisterContentObserver(this);
                    mShowMainActivityManager.setShouldAction(true);
                } else if(s.syncstate==SyncState.FAILED) {
                    getContext().getContentResolver().unregisterContentObserver(this);
                    RocketChatWSService.kill(getContext());
                    showErrorFragment("Failed to connect.");
                }
            }
        });
    }

    private void showErrorFragment(String msg) {
        getFragmentManager().beginTransaction()
                .remove(this)
                .replace(R.id.simple_framelayout, LoginErrorFragment.create(msg))
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entry_screen, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mShowMainActivityManager.setConstrainedMet(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mShowMainActivityManager.setConstrainedMet(false);
    }
}
