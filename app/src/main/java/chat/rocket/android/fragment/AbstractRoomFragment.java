package chat.rocket.android.fragment;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Room;

public abstract class AbstractRoomFragment extends AbstractFragment {
    protected View mRootView;
    protected String mHost;
    protected long mRoomBaseId;
    protected String mRoomId;
    protected String mRid;
    protected String mRoomName;
    protected Room.Type mRoomType;

    protected void initFromArgs(Bundle args) {
        mHost = args.getString("host");
        mRoomBaseId = args.getLong("roomBaseId");
        mRoomId = args.getString("roomId");
        mRid = args.getString("rid");
        mRoomName = args.getString("roomName");
        mRoomType = Room.Type.getType(args.getString("roomType"));
    }

    protected boolean hasValidArgs(Bundle args) {
        if (args == null) return false;
        return args.containsKey("host")
                && args.containsKey("roomBaseId")
                && args.containsKey("roomId")
                && args.containsKey("rid")
                && args.containsKey("roomName")
                && args.containsKey("roomType");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (!hasValidArgs(args)) {
            throw new IllegalArgumentException("Params 'roomId' and 'roomName' are required for creating AbstractRoomFragment");
        }
        initFromArgs(args);

        final Uri uri = RocketChatProvider.getUriForQuery(Room.TABLE_NAME, mRoomBaseId);
        getContext().getContentResolver().registerContentObserver(uri, false, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                if (getContext() == null) return;
                Cursor c = getContext().getContentResolver().query(uri, null, null, null, null);
                if (c == null) return;
                if (c.moveToFirst()) {
                    final Room r = Room.createFromCursor(c);
                    if (r != null) {
                        if (mRootView != null) {
                            mRootView.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mRoomName != r.name) {
                                        mRoomName = r.name;
                                        setupToolbar();
                                    }
                                    if (mRoomType != r.type) {
                                        mRoomType = r.type;
                                        setupToolbar();
                                    }
                                }
                            });
                            onRoomLoaded(r);
                        }
                    }
                }
                c.close();
            }
        });
    }

    protected void onRoomLoaded(final Room r) {
    }

    protected void initializeToolbar() {
        Toolbar bar = (Toolbar) mRootView.findViewById(R.id.toolbar_chatroom);

        setHasOptionsMenu(true);
        getAppCompatActivity().setSupportActionBar(bar);
        if (getAppCompatActivity().getSupportActionBar() != null) {
            getAppCompatActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // REMARK! setNavigationOnClickListener works only after setDisplayHomeAsUpEnabled(true) is called.
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupToolbar();
    }

    protected void setupToolbar() {
        Toolbar bar = (Toolbar) mRootView.findViewById(R.id.toolbar_chatroom);
        bar.setTitle(mRoomName);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    protected final void focusToEditor(TextView editor) {
        editor.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT);
    }

    protected final void unFocusEditor() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mRootView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
