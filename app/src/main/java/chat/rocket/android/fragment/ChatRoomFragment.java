package chat.rocket.android.fragment;

import android.content.Context;
import android.database.Cursor;
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

import org.json.JSONArray;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.Constants;
import chat.rocket.android.R;
import chat.rocket.android.activity.OnBackPressListener;
import chat.rocket.android.api.Auth;
import chat.rocket.android.api.RocketChatRestAPI;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.User;
import chat.rocket.android.view.CursorRecyclerViewAdapter;
import chat.rocket.android.view.MessageComposer;
import ollie.query.Select;

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
        ServerConfig s = Select.from(ServerConfig.class).where("is_primary = 1").fetchSingle();
        if (s == null) return;

        new RocketChatRestAPI(s.hostname)
            .listRecentMessages(new Auth(s.authUserId, s.authToken), mRoomId)
            .onSuccess(new Continuation<JSONArray, Object>() {
                @Override
                public Object then(Task<JSONArray> task) throws Exception {
                    JSONArray messages = task.getResult();
                    for (int i = 0; i < messages.length(); i++) {
                        JSONObject message = messages.getJSONObject(i);

                        String messageId = message.getString("_id");
                        Message m = Select.from(Message.class).where("cid = ?", messageId).fetchSingle();
                        if (m == null) {
                            m = new Message();
                            m._id = messageId;
                        }
                        m.roomId = message.getString("rid");
                        m.content = message.getString("msg");
                        m.timestamp = message.getString("ts");

                        JSONObject user = message.getJSONObject("u");
                        String userId = user.getString("_id");
                        User u = Select.from(User.class).where("cid = ?", userId).fetchSingle();
                        if (u == null) {
                            u = new User();
                            u._id = userId;
                        }
                        u.name = user.getString("username");
                        u.save();

                        m.userId = userId;
                        m.save();

                        //notify change to observers.
                        Uri uri = Uri.parse("content://chat.rocket.android/message/" + m.id);
                        getContext().getContentResolver().notifyChange(uri, null);
                    }

                    return null;
                }
            });
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
                Uri uri = Uri.parse("content://chat.rocket.android/message");
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
            Message m = Message.fromCursor(cursor);
            User u = Select.from(User.class).where("cid=?", m.userId).fetchSingle();

            viewHolder.content.setText(m.content);
            viewHolder.username.setText(u.name);
            viewHolder.timestamp.setText(Constants.DATETIME_FORMAT.parseDateTime(m.timestamp).toString());
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
        final int margin = getActivity().getResources().getDimensionPixelSize(R.dimen.margin_normal);

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

        ServerConfig s = Select.from(ServerConfig.class).where("is_primary = 1").fetchSingle();
        if (s == null) return;

        composer.setEnabled(false);

        new RocketChatRestAPI(s.hostname)
                .sendMessage(new Auth(s.authUserId, s.authToken), mRoomId, message)
                .continueWith(new Continuation<Boolean, Object>() {
                    @Override
                    public Object then(Task<Boolean> task) throws Exception {
                        final boolean failed = task.isFaulted();
                        mRootView.post(new Runnable() {
                            @Override
                            public void run() {
                                composer.setEnabled(true);

                                if (!failed) {
                                    fetchNewMessages();
                                    composer.setText("");
                                }
                            }
                        });
                        return null;
                    }
                })
                .continueWith(new Continuation<Object, Object>() {
                    @Override
                    public Object then(Task<Object> task) throws Exception {
                        if(task.isFaulted()) {
                            Log.e(Constants.LOG_TAG, "error", task.getError());
                        }
                        return null;
                    }
                });
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
