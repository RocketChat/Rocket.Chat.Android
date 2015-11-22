package chat.rocket.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.ViewUtil;

public class LoginErrorFragment extends AbstractFragment {
    public LoginErrorFragment(){}

    private String mErrMessage;

    public static LoginErrorFragment create(String errMessage) {
        Bundle args = new Bundle();
        args.putString("msg", errMessage);
        LoginErrorFragment f = new LoginErrorFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args==null || !args.containsKey("msg")) {
            throw new IllegalArgumentException("A param 'msg' is required to use LoginErrorFragment");
        }

        mErrMessage = args.getString("msg");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.entry_error_screen, container, false);

        ((TextView) root.findViewById(R.id.txt_login_error_reason)).setText(mErrMessage);

        root.findViewById(R.id.btn_retry_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSplashFragment();
            }
        });

        ViewUtil.setClickable((TextView) root.findViewById(R.id.link_login_with_another_account),
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSelectAccountFragment();
                }
            });

        return root;
    }

    private void showSplashFragment() {
        getFragmentManager().beginTransaction()
                .remove(this)
                .replace(R.id.simple_framelayout, new SplashFragment())
                .commit();
    }

    private void showSelectAccountFragment() {
        getFragmentManager().beginTransaction()
                .remove(this)
                .replace(R.id.simple_framelayout, new SelectAccountFragment())
                .commit();
    }
}
