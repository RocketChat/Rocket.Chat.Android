package chat.rocket.android.fragment.server_config;


import android.content.Context;

import java.util.List;

import chat.rocket.android.shared.BaseContract;
import chat.rocket.core.models.LoginServiceConfiguration;

public interface LoginContract {

    interface View extends BaseContract.View {

        void showLoader();

        void hideLoader();

        void showErrorInUsernameEditText();

        void showErrorInPasswordEditText();

        void showError(String message);

        void showLoginServices(List<LoginServiceConfiguration> loginServiceList);

        void showTwoStepAuth();

        void goBack();
    }

    interface Presenter extends BaseContract.Presenter<View> {

        void login(String username, String password);

        void goBack(Context ctx);
    }
}
