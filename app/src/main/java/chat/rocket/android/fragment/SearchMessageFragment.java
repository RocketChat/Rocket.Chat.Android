package chat.rocket.android.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import chat.rocket.android.R;

public class SearchMessageFragment extends AbstractRoomFragment {
    public SearchMessageFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.message_search_screen, container, false);

        initializeToolbar();
        initializeSearchBox();

        return mRootView;
    }

    private void initializeSearchBox() {
        final TextView searchView = (TextView) mRootView.findViewById(R.id.message_search_text);
        searchView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(right-left>0 && bottom-top>0) {
                    focusToEditor(searchView);
                    v.removeOnLayoutChangeListener(this);
                }
            }
        });
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(EditorInfo.IME_ACTION_SEARCH==actionId) {
                    search(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                scheduleSearch(s.toString());
            }
        });
    }

    private final static int MSG_DO_SEARCH = 12341;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what!=MSG_DO_SEARCH) return;

            search((String)msg.obj);
        }
    };

    private void scheduleSearch(String query) {
        unscheduleSearch();
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_DO_SEARCH, query), 1200);
    }

    private void unscheduleSearch() {
        if (mHandler.hasMessages(MSG_DO_SEARCH)) mHandler.removeMessages(MSG_DO_SEARCH);
    }

    private void search(String query) {
        Toast.makeText(getContext(), "messageSearch["+query+","+mRoomId+"] NOT implemented!",Toast.LENGTH_SHORT).show();
        unscheduleSearch();
    }

    @Override
    public void onDestroyView() {
        unFocusEditor();
        super.onDestroyView();
    }

}
