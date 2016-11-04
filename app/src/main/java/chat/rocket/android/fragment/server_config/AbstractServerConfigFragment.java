package chat.rocket.android.fragment.server_config;

import android.os.Bundle;
import android.support.annotation.Nullable;

import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.helper.TextUtils;

abstract class AbstractServerConfigFragment extends AbstractFragment {
    protected String mServerConfigId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args == null) {
            finish();
            return;
        }

        mServerConfigId = args.getString("id");
        if (TextUtils.isEmpty(mServerConfigId)) {
            finish();
            return;
        }
    }
}
