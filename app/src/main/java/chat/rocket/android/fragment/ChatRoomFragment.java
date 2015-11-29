package chat.rocket.android.fragment;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.Constants;
import chat.rocket.android.DateTime;
import chat.rocket.android.R;
import chat.rocket.android.activity.OnBackPressListener;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.MethodCall;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.User;
import chat.rocket.android.view.CursorRecyclerViewAdapter;
import chat.rocket.android.view.MessageComposer;

public class ChatRoomFragment extends AbstractFragment implements OnBackPressListener{
    private static final int LOADER_ID = 0x12346;

    public ChatRoomFragment(){}

    private String mRoomId;
    private String mRoomName;
    private View mRootView;
    private MessageAdapter mAdapter;

    public static ChatRoomFragment create(String roomId, String roomName) {
        ChatRoomFragment f = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString("roomId", roomId);
        args.putString("roomName", roomName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(!hasValidArgs(args)) {
            throw new IllegalArgumentException("Params 'roomId' and 'roomName' are required for creating ChatRoomFragment");
        }
        mRoomId = args.getString("roomId");
        mRoomName = args.getString("roomName");
    }

    private boolean hasValidArgs(Bundle args) {
        if(args == null) return false;
        return args.containsKey("roomId") && args.containsKey("roomName");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.chat_room_screen, container, false);

        setupToolbar();
        setupListView();
        loadMessages();
        fetchNewMessages();
        setupMessageComposer();
        return mRootView;
    }

    private void setupToolbar(){
        Toolbar bar = (Toolbar) mRootView.findViewById(R.id.toolbar_chatroom);
        bar.setTitle(mRoomName);
    }

    private void fetchNewMessages() {
        ServerConfig s = getPrimaryServerConfig();
        if (s == null) return;

        try {
            MethodCall
                    .create("loadMessages", new JSONObject().put("room_id",mRoomId))
                    .putByContentProvider(getContext());
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "error", e);
        }
    }

    private void setupListView() {
        final Context context = mRootView.getContext();
        mAdapter = new MessageAdapter(context, null);
        final RecyclerView messageListView = (RecyclerView) mRootView.findViewById(R.id.listview_messages);
        messageListView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true));
        messageListView.setAdapter(mAdapter);
    }

    private void loadMessages(){
        final Context context = mRootView.getContext();
        getLoaderManager().restartLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri = RocketChatProvider.getUriForQuery(Message.TABLE_NAME);
                return new CursorLoader(context, uri, null, "room_id = ?", new String[]{mRoomId}, "timestamp DESC");
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapCursor(null);
            }
        });
    }

    private static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView timestamp;
        TextView content;

        public MessageViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.list_item_message_username);
            timestamp = (TextView) itemView.findViewById(R.id.list_item_message_timestamp);
            content = (TextView) itemView.findViewById(R.id.list_item_message_content);
        }
    }

    private class MessageAdapter extends CursorRecyclerViewAdapter<MessageViewHolder> {
        private static final int DUMMY_HEADER = 100;

        private LayoutInflater mInflater;
        private int mHeaderHeight;


        public MessageAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            mInflater = LayoutInflater.from(context);
        }

        public int getBasicItemCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public int getItemCount() {
            return getBasicItemCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return DUMMY_HEADER;
            }
            return super.getItemViewType(position);
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType==DUMMY_HEADER) {
                final View v = new View(parent.getContext());
                v.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,mHeaderHeight));
                return new MessageViewHolder(v);
            }
            return new MessageViewHolder(mInflater.inflate(R.layout.listitem_message, parent, false));
        }

        @Override
        public void onBindViewHolder(MessageViewHolder viewHolder, int position) {
            if (position == 0) {
                if(viewHolder.itemView.getHeight()!=mHeaderHeight) {
                    viewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeaderHeight));
                }
                return;
            }

            super.onBindViewHolder(viewHolder, position-1);
        }

        @Override
        public void bindView(MessageViewHolder viewHolder, Context context, Cursor cursor) {
            final Message m = Message.createFromCursor(cursor);
            User u = RocketChatDatabaseHelper.read(context, new RocketChatDatabaseHelper.DBCallback<User>() {
                @Override
                public User process(SQLiteDatabase db) throws JSONException {
                    return User.getById(db, m.userId);
                }
            });

            viewHolder.content.setText(m.content);
            viewHolder.username.setText(u.name);
            viewHolder.timestamp.setText(DateTime.fromEpocMs(m.timestamp, DateTime.Format.AUTO_DAY_TIME));
        }

        public void setHeaderHeight(int height){
            mHeaderHeight = height;
            notifyItemChanged(0);
        }
    }

    private MessageComposer getMessageComposer(){
        return (MessageComposer) mRootView.findViewById(R.id.message_composer);
    }

    private FloatingActionButton getButtonCompose(){
        return (FloatingActionButton) mRootView.findViewById(R.id.chat_btn_compose);
    }

    private void setupMessageComposer(){
        final MessageComposer composer = getMessageComposer();
        final FloatingActionButton btnCompose = getButtonCompose();
        final int margin = getContext().getResources().getDimensionPixelSize(R.dimen.margin_normal);

        btnCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMessageComposerVisibility(true);
            }
        });

        btnCompose.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mAdapter.setHeaderHeight(bottom - top + margin);
                btnCompose.removeOnLayoutChangeListener(this);
            }
        });

        composer.setOnActionListener(new MessageComposer.ActionListener() {
            @Override
            public void onSubmit(String message) {
                sendMessage(message);
            }

            @Override
            public void onCancel() {
                setMessageComposerVisibility(false);
            }
        });
    }

    private void sendMessage(String message) {
        final MessageComposer composer = getMessageComposer();
        if(TextUtils.isEmpty(message)) return;

        ServerConfig s = getPrimaryServerConfig();
        if (s == null) return;

        composer.setEnabled(false);

        try {
            // TODO: should replace this implementation to insert new Message (not method call)!!
            final Uri uri = MethodCall
                    .create("sendMessage", new JSONObject().put("room_id",mRoomId).put("msg", message))
                    .putByContentProvider(getContext());

            getContext().getContentResolver().registerContentObserver(uri, false, new ContentObserver(null) {
                @Override
                public void onChange(boolean selfChange) {
                    Cursor c = getContext().getContentResolver().query(uri, null,null,null,null);
                    if(c==null || c.getCount()==0) {
                        //deleted
                        enableComposer(false);
                        getContext().getContentResolver().unregisterContentObserver(this);
                    } else if(c.moveToFirst()) {
                        MethodCall m = MethodCall.createFromCursor(c);
                        if(m.syncstate == SyncState.SYNCED || m.syncstate == SyncState.FAILED) {
                            enableComposer(m.syncstate == SyncState.SYNCED);
                            getContext().getContentResolver().unregisterContentObserver(this);
                            fetchNewMessages();
                            m.deleteByContentProvider(getContext());
                        }
                    }
                }

                private void enableComposer(final boolean clear){
                    composer.post(new Runnable() {
                        @Override
                        public void run() {
                            if(clear) composer.setText("");
                            composer.setEnabled(true);
                        }
                    });
                }
            });
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "error", e);
        }
    }

    private void setMessageComposerVisibility(boolean visible) {
        final FloatingActionButton btnCompose = getButtonCompose();
        final MessageComposer composer = getMessageComposer();

        if(visible) {
            btnCompose.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    composer.show(null);

                }
            });
        }
        else{
            composer.hide(new Runnable() {
                @Override
                public void run() {
                    btnCompose.show();
                }
            });
        }
    }

    @Override
    public boolean onBackPressed() {
        MessageComposer composer = getMessageComposer();
        if(composer.isShown()) {
            setMessageComposerVisibility(false);
            return true;
        }
        return false;
    }
}
