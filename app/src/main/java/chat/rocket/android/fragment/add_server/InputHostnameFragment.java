package chat.rocket.android.fragment.add_server;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import chat.rocket.android.BuildConfig;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.service.ConnectivityManager;

/**
 * Input server host.
 */
public class InputHostnameFragment extends AbstractFragment implements InputHostnameContract.View {

    private InputHostnameContract.Presenter presenter;
    private ConstraintLayout container;
    private View waitingView;

    public InputHostnameFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context appContext = getContext().getApplicationContext();
        presenter = new InputHostnamePresenter(ConnectivityManager.getInstance(appContext));
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_input_hostname;
    }

    @Override
    protected void onSetupView() {
        setupVersionInfo();

        container = rootView.findViewById(R.id.container);
        waitingView = rootView.findViewById(R.id.waiting);
        rootView.findViewById(R.id.btn_connect).setOnClickListener(view -> handleConnect());
    }

    private void setupVersionInfo() {
        TextView versionInfoView = rootView.findViewById(R.id.version_info);
        versionInfoView.setText(getString(R.string.version_info_text, BuildConfig.VERSION_NAME));
    }

    private void handleConnect() {
        hideSoftKeyboard();
        presenter.connectTo(getHostname());
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.bindView(this);
    }

    @Override
    public void onDestroyView() {
        presenter.release();
        super.onDestroyView();
    }

    private String getHostname() {
        final TextView editor = (TextView) rootView.findViewById(R.id.editor_hostname);

        return TextUtils.or(TextUtils.or(editor.getText(), editor.getHint()), "").toString().toLowerCase();
    }

    private void showError(String errString) {
        Snackbar.make(rootView, errString, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showLoader() {
        container.setVisibility(View.GONE);
        waitingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader(Boolean isValidServerUrl) {
        if(!isValidServerUrl) {
            waitingView.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showInvalidServerError() {
        showError(getString(R.string.input_hostname_invalid_server_message));
    }

    @Override
    public void showConnectionError() {
        showError(getString(R.string.connection_error_try_later));
    }

    @Override
    public void showHome() {
        LaunchUtil.showMainActivity(getContext());
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
