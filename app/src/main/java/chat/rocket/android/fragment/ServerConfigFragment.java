package chat.rocket.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import chat.rocket.android.R;
import chat.rocket.android.ViewUtil;

public class ServerConfigFragment extends AbstractFragment {

    private View mRootView;

    public ServerConfigFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.serverconfig_screen, container, false);

        mRootView.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addServerConfig();
            }
        });

        return mRootView;
    }

    private void addServerConfig(){
        final String host = ViewUtil.getText(mRootView, R.id.txt_login_serverhost).toString();
        final String account = ViewUtil.getText(mRootView, R.id.txt_login_account).toString();
        final String password = ViewUtil.getText(mRootView, R.id.txt_login_password).toString();

        getFragmentManager().beginTransaction()
                .replace(R.id.simple_framelayout, AuthenticateFragment.create(host,account,password))
                .addToBackStack(null)
                .commit();
    }
}
