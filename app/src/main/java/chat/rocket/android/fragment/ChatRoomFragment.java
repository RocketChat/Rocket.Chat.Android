package chat.rocket.android.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
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
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emojione.Emojione;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

import chat.rocket.android.Constants;
import chat.rocket.android.DateTime;
import chat.rocket.android.R;
import chat.rocket.android.activity.OnBackPressListener;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.MethodCall;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.User;
import chat.rocket.android.preference.Cache;
import chat.rocket.android.view.Avatar;
import chat.rocket.android.view.CursorRecyclerViewAdapter;
import chat.rocket.android.view.Linkify;
import chat.rocket.android.view.LoadMoreScrollListener;
import chat.rocket.android.view.MessageComposer;

public class ChatRoomFragment extends AbstractFragment implements OnBackPressListener{
    private static final int LOADER_ID = 0x12346;
    private static final int PICK_IMAGE_ID = 0x11;

    public ChatRoomFragment(){}

    private String mHost;
    private long mRoomBaseId;
    private String mRoomId;
    private String mRoomName;
    private Room.Type mRoomType;
    private boolean mRoomHasMore;
    private String mUserId;
    private String mUsername;
    private View mRootView;
    private MessageAdapter mAdapter;
    private LoadMoreScrollListener mLoadMoreListener;

    private static final HashSet<String> sInlineViewSupportedMime = new HashSet<String>(){
        {
            add("image/png");
            add("image/jpg");
            add("image/jpeg");
            add("image/webp");
        }
    };

    public static ChatRoomFragment create(String host, Room r) {
        ChatRoomFragment f = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString("host", host);
        args.putLong("roomBaseId", r._id);
        args.putString("roomId", r.id);
        args.putString("roomName", r.name);
        args.putString("roomType", r.type.getValue());
        args.putBoolean("roomHasMore", r.hasMore);
        f.setArguments(args);
        return f;
    }

    private void initFromArgs(Bundle args) {
        mHost = args.getString("host");
        mRoomBaseId = args.getLong("roomBaseId");
        mRoomId = args.getString("roomId");
        mRoomName = args.getString("roomName");
        mRoomType = Room.Type.getType(args.getString("roomType"));
        mRoomHasMore = args.getBoolean("roomHasMore");
    }

    private boolean hasValidArgs(Bundle args) {
        if(args == null) return false;
        return args.containsKey("host")
                && args.containsKey("roomBaseId")
                && args.containsKey("roomId")
                && args.containsKey("roomName")
                && args.containsKey("roomType")
                && args.containsKey("roomHasMore");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(!hasValidArgs(args)) {
            throw new IllegalArgumentException("Params 'roomId' and 'roomName' are required for creating ChatRoomFragment");
        }
        initFromArgs(args);
        mUserId = Cache.get(getContext()).getString(Cache.KEY_MY_USER_ID,"");
        mUsername = Cache.get(getContext()).getString(Cache.KEY_MY_USER_NAME,"");
        if(TextUtils.isEmpty(mUsername)) {
            Cache.waitForValue(getContext(), Cache.KEY_MY_USER_ID, new Cache.ValueCallback<String>() {
                @Override
                public void onGetValue(String value) {
                    mUserId = value;
                }
            });
            Cache.waitForValue(getContext(), Cache.KEY_MY_USER_NAME, new Cache.ValueCallback<String>() {
                @Override
                public void onGetValue(String value) {
                    mUsername = value;
                }
            });
        }

        final Uri uri = RocketChatProvider.getUriForQuery(Room.TABLE_NAME, mRoomBaseId);
        getContext().getContentResolver().registerContentObserver(uri, false, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                if(getContext()==null) return;
                Cursor c = getContext().getContentResolver().query(uri, null,null,null,null);
                if(c!=null && c.moveToFirst()) {
                    final Room r = Room.createFromCursor(c);
                    if(r!=null){
                        if(r.alert) {
                            r.alert = false;
                            r.syncstate = SyncState.NOT_SYNCED;
                            r.putByContentProvider(getContext());
                        }
                        if(mRootView!=null) {
                            mRootView.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(mRoomName != r.name){
                                        mRoomName = r.name;
                                        setupToolbar();
                                    }
                                    if(mRoomType != r.type){
                                        mRoomType = r.type;
                                        setupToolbar();
                                    }
                                    if(mRoomHasMore != r.hasMore){
                                        mRoomHasMore = r.hasMore;
                                        setupListViewHasMore();
                                    }
                                }
                            });
                        }
                    }
                    c.close();
                }
            }
        });
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
        setupUploader();
        return mRootView;
    }

    private void setupToolbar(){
        Toolbar bar = (Toolbar) mRootView.findViewById(R.id.toolbar_chatroom);
        bar.setTitle(mRoomName);
        switch (mRoomType){
            case CHANNEL:
                bar.setNavigationIcon(R.drawable.ic_room_type_channel);
                break;
            case PRIVATE_GROUP:
                bar.setNavigationIcon(R.drawable.ic_room_type_private);
                break;
            case DIRECT_MESSAGE:
                bar.setNavigationIcon(R.drawable.ic_room_type_direct);
                break;
            default:
                bar.setNavigationIcon(null);
        }
    }

    private void fetchNewMessages() {
        ServerConfig s = getPrimaryServerConfig();
        if (s == null) return;

        try {
            MethodCall
                    .create("loadMessages", new JSONObject().put("room_id",mRoomId).put("clean",true))
                    .putByContentProvider(getContext());
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "error", e);
        }
    }

    private void fetchMoreMessages() {
        Message m = RocketChatDatabaseHelper.read(getContext(), new RocketChatDatabaseHelper.DBCallback<Message>() {
            @Override
            public Message process(SQLiteDatabase db) throws Exception {
                return Message.get(db, "room_id = ?",new String[]{mRoomId},"timestamp ASC");
            }
        });
        if(m==null) {
            return;
        }

        try {
            final Uri uri = MethodCall
                    .create("loadMessages", new JSONObject().put("room_id",mRoomId).put("end_ts",m.timestamp))
                    .putByContentProvider(getContext());
            getContext().getContentResolver().registerContentObserver(uri, false, new ContentObserver(null) {
                @Override
                public void onChange(boolean selfChange) {
                    Cursor c = getContext().getContentResolver().query(uri,null,null,null,null);
                    if(c==null){
                        getContext().getContentResolver().unregisterContentObserver(this);
                    }
                    if(c!=null && c.moveToFirst()){
                        MethodCall m = MethodCall.createFromCursor(c);
                        getContext().getContentResolver().unregisterContentObserver(this);
                    }
                    if(mLoadMoreListener!=null) mLoadMoreListener.setLoadingDone();
                }
            });
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "error", e);
        }
    }

    private void setupListView() {
        final Context context = mRootView.getContext();
        mAdapter = new MessageAdapter(context, null);
        final RecyclerView messageListView = (RecyclerView) mRootView.findViewById(R.id.listview_messages);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true);
        messageListView.setLayoutManager(layoutManager);
        messageListView.setAdapter(mAdapter);

        mLoadMoreListener = new LoadMoreScrollListener(layoutManager, 20) {
            @Override
            public void requestMoreItem() {
                if(mRoomHasMore) fetchMoreMessages();
            }
        };
        messageListView.addOnScrollListener(mLoadMoreListener);
        messageListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(top < bottom && left < right){
                    setupListViewHasMore();
                    v.removeOnLayoutChangeListener(this);
                }
            }
        });
    }

    private void setupListViewHasMore(){
        mAdapter.setHasMore(mRoomHasMore);
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
        Avatar avatar;
        TextView username;
        TextView timestamp;
        TextView content;
        LinearLayout inlineContainer;
        View newDayContainer;
        TextView newDayText;
        View usertimeContainer;

        public MessageViewHolder(View itemView, String host) {
            super(itemView);
            avatar = new Avatar(host, itemView.findViewById(R.id.avatar_color), (TextView)itemView.findViewById(R.id.avatar_initials), (ImageView)itemView.findViewById(R.id.avatar_img));
            username = (TextView) itemView.findViewById(R.id.list_item_message_username);
            timestamp = (TextView) itemView.findViewById(R.id.list_item_message_timestamp);
            content = (TextView) itemView.findViewById(R.id.list_item_message_content);
            inlineContainer = (LinearLayout) itemView.findViewById(R.id.list_item_inline_container);
            newDayContainer = itemView.findViewById(R.id.list_item_message_newday);
            newDayText = (TextView) itemView.findViewById(R.id.list_item_message_newday_text);
            usertimeContainer = itemView.findViewById(R.id.list_item_message_user_time_container);
        }
    }

    private class MessageAdapter extends CursorRecyclerViewAdapter<MessageViewHolder> {
        private static final int DUMMY_HEADER = 100;
        private static final int DUMMY_FOOTER = 101;

        private LayoutInflater mInflater;
        private int mHeaderHeight;
        private boolean mHasMore;


        public MessageAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            mInflater = LayoutInflater.from(context);
        }

        public int getBasicItemCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public int getItemCount() {
            return getBasicItemCount() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return DUMMY_HEADER;
            }
            else if (position == getItemCount()-1) {
                return DUMMY_FOOTER;
            }
            return super.getItemViewType(position);
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType==DUMMY_HEADER) {
                final View v = new View(parent.getContext());
                v.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,mHeaderHeight));
                return new MessageViewHolder(v, mHost);
            }
            else if(viewType==DUMMY_FOOTER) {
                return new MessageViewHolder(mInflater.inflate(R.layout.listitem_start_of_conversation, parent, false), mHost);
            }
            return new MessageViewHolder(mInflater.inflate(R.layout.listitem_message, parent, false),mHost);
        }

        @Override
        public void onBindViewHolder(MessageViewHolder viewHolder, int position) {
            int type = getItemViewType(position);
            if (type == DUMMY_HEADER) {
                if(viewHolder.itemView.getHeight()!=mHeaderHeight) {
                    viewHolder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeaderHeight));
                }
                return;
            }
            else if(type == DUMMY_FOOTER) {
                viewHolder.itemView.setVisibility(mHasMore? View.GONE : View.VISIBLE);
                return;
            }

            super.onBindViewHolder(viewHolder, position-1);
        }

        @Override
        public void bindView(MessageViewHolder viewHolder, Context context, int position, Cursor cursor) {
            final Message m = Message.createFromCursor(cursor);

            Message nextM = null;
            if(position < getBasicItemCount()-1) {
                if(cursor.moveToPosition(position+1)){
                    nextM = Message.createFromCursor(cursor);
                }
            }
            setSequentialOrNewDateIfNeeded(viewHolder, context, nextM, m);

            User u = RocketChatDatabaseHelper.read(context, new RocketChatDatabaseHelper.DBCallback<User>() {
                @Override
                public User process(SQLiteDatabase db) throws JSONException {
                    return User.getById(db, m.userId);
                }
            });

            if(u!=null) viewHolder.avatar.setForUser(u.name);

            //see Rocket.Chat:packages/rocketchat-lib/client/MessageTypes.coffee
            String username = (u!=null)? u.getDisplayName() : "unknown user";
            boolean systemMsg = true;
            switch (m.type) {
                case USER_JOINED:
                    viewHolder.content.setText("(has joined the channel)");
                    break;
                case USER_LEFT:
                    viewHolder.content.setText("(has left the channel)");
                    break;

                case USER_ADDED:
                    viewHolder.content.setText(String.format("(User %s added by %s)",m.content,username));
                    break;

                case USER_REMOVED:
                    viewHolder.content.setText(String.format("(User %s removed by %s)",m.content,username));
                    break;

                case ROOM_NAME_CHANGED:
                    viewHolder.content.setText(String.format("(Room name changed to: %s by %s)",m.content,username));
                    break;

                case MESSAGE_REMOVED:
                    viewHolder.content.setText("(message removed)");
                    break;

                case WELCOME:
                    viewHolder.content.setText(String.format("Welcome %s!",username));
                    break;


                case UNSPECIFIED:
                default:
                    viewHolder.content.setText(Emojione.shortnameToUnicode(m.content,false));
                    Linkify.markupSync(viewHolder.content);
                    systemMsg = false;
            }
            if(systemMsg) {
                viewHolder.content.setTypeface(null, Typeface.ITALIC);
                viewHolder.content.setEnabled(false);
            }
            else {
                viewHolder.content.setTypeface(null, Typeface.NORMAL);
                viewHolder.content.setEnabled(true);
            }

            if(u!=null) viewHolder.username.setText(u.getDisplayName());
            viewHolder.timestamp.setText(DateTime.fromEpocMs(m.timestamp, DateTime.Format.TIME));

            viewHolder.inlineContainer.removeAllViews();

            if(!TextUtils.isEmpty(m.urls)) {
                try {
                    JSONArray urls = new JSONArray(m.urls);
                    for (int i = 0; i < urls.length(); i++) {
                        insertUrl(viewHolder, urls.getJSONObject(i));
                    }
                }
                catch (Exception e) {
                    Log.e(Constants.LOG_TAG, "error", e);
                }
            }

        }

        private void setSequentialOrNewDateIfNeeded(MessageViewHolder viewHolder, Context context, @Nullable Message nextM, Message m) {
            //see Rocket.Chat:packages/rocketchat-livechat/app/client/views/message.coffee
            if(nextM==null || !DateTime.fromEpocMs(nextM.timestamp, DateTime.Format.DATE)
                    .equals(DateTime.fromEpocMs(m.timestamp, DateTime.Format.DATE))) {
                setNewDay(viewHolder, DateTime.fromEpocMs(m.timestamp, DateTime.Format.DATE));
                setSequential(viewHolder, false);
            }
            else if(!nextM.userId.equals(m.userId)) {
                setNewDay(viewHolder, null);
                setSequential(viewHolder, false);
            }
            else{
                setNewDay(viewHolder, null);
                setSequential(viewHolder, true);
            }


        }

        private void setSequential(MessageViewHolder viewHolder, boolean sequential) {
            if(sequential){
                viewHolder.avatar.hide();
                viewHolder.usertimeContainer.setVisibility(View.GONE);
            }
            else{
                viewHolder.avatar.show();
                viewHolder.usertimeContainer.setVisibility(View.VISIBLE);
            }
        }

        private void setNewDay(MessageViewHolder viewHolder, @Nullable String newDayText) {
            if(TextUtils.isEmpty(newDayText)) viewHolder.newDayContainer.setVisibility(View.GONE);
            else {
                viewHolder.newDayText.setText(newDayText);
                viewHolder.newDayContainer.setVisibility(View.VISIBLE);
            }
        }

        private void insertUrl(MessageViewHolder viewHolder, JSONObject urlObj) throws JSONException {
            if (urlObj.isNull("headers")) return;
            JSONObject header = urlObj.getJSONObject("headers");
            final String url = urlObj.getString("url");

            if (header.isNull("contentType")) return;
            String contentType = header.getString("contentType");

            final Context context = viewHolder.itemView.getContext();
            if (contentType.startsWith("image/") && sInlineViewSupportedMime.contains(contentType)) {
                View v = LayoutInflater.from(context).inflate(R.layout.listitem_inline_image,viewHolder.inlineContainer,false);
                ImageView img = (ImageView) v.findViewById(R.id.list_item_inline_image);
                Picasso.with(context)
                        .load(url)
                        .placeholder(R.drawable.image_dummy)
                        .error(R.drawable.image_error)
                        .into(img);
                viewHolder.inlineContainer.addView(v);
            }

            // see Rocket.Chat:packages/rocketchat-oembed/client/oembedUrlWidget.coffee
            if (!urlObj.isNull("meta")) {
                JSONObject meta =urlObj.getJSONObject("meta");

                String title = null;
                if(!meta.isNull("ogTitle")) title = meta.getString("ogTitle");
                else if(!meta.isNull("twitterTitle")) title = meta.getString("twitterTitle");
                else if(!meta.isNull("pageTitle")) title = meta.getString("pageTitle");

                String description = null;
                if(!meta.isNull("ogDescription")) description = meta.getString("ogDescription");
                else if(!meta.isNull("twitterDescription")) description = meta.getString("twitterDescription");
                else if(!meta.isNull("description")) description = meta.getString("description");

                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) return;

                if (description.startsWith("\"")) description = description.substring(1);
                if (description.endsWith("\"")) description = description.substring(0, description.length()-1);

                String imageURL = null;
                if(!meta.isNull("ogImage")) imageURL = meta.getString("ogImage");
                else if(!meta.isNull("twitterImage")) imageURL = meta.getString("twitterImage");

                String host = urlObj.getJSONObject("parsedUrl").getString("host");

                View v = LayoutInflater.from(context).inflate(R.layout.listitem_inline_embed_url,viewHolder.inlineContainer,false);

                ((TextView) v.findViewById(R.id.inline_embed_url_host)).setText(host);
                ((TextView) v.findViewById(R.id.inline_embed_url_title)).setText(title);
                ((TextView) v.findViewById(R.id.inline_embed_url_description)).setText(description);


                ImageView img = (ImageView) v.findViewById(R.id.inline_embed_url_image);
                if(TextUtils.isEmpty(imageURL)) img.setVisibility(View.GONE);
                else {
                    Picasso.with(context)
                            .load(imageURL)
                            .placeholder(R.drawable.image_dummy)
                            .error(R.drawable.image_error)
                            .into(img);
                    img.setVisibility(View.VISIBLE);
                }

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });

                viewHolder.inlineContainer.addView(v);
            }
        }

        public void setHeaderHeight(int height){
            mHeaderHeight = height;
            notifyItemChanged(0);
        }

        public void setHasMore(boolean hasMore) {
            mHasMore = hasMore;
            //notifyItemChanged(getItemCount()-1);
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

        Message m = new Message();
        m.syncstate = SyncState.NOT_SYNCED;
        m.content = message;
        m.roomId = mRoomId;
        m.userId = mUserId;
        m.putByContentProvider(getContext());

        composer.setText("");
        composer.setEnabled(true);

    }

    private void setMessageComposerVisibility(boolean visible) {
        final FloatingActionButton btnCompose = getButtonCompose();
        final FloatingActionButton btnUploadFile = getButtonUploadFile();
        final MessageComposer composer = getMessageComposer();

        if(visible) {
            btnUploadFile.hide();
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
                    btnUploadFile.show();
                }
            });
        }
    }

    private FloatingActionButton getButtonUploadFile(){
        return (FloatingActionButton) mRootView.findViewById(R.id.chat_btn_upload);
    }

    private void setupUploader(){
        getButtonUploadFile().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture to Upload"), PICK_IMAGE_ID);
            }
        });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode!=PICK_IMAGE_ID) return;
        if(resultCode != Activity.RESULT_OK || data==null) return;

        Uri uriToObserve = null;
        try {
            Uri uri = data.getData();
            Cursor c = getContext().getContentResolver().query(uri, null, null, null, null);
            if(c!=null && c.moveToFirst()) {
                String filename = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                long filesize = c.getLong(c.getColumnIndex(OpenableColumns.SIZE));

                JSONObject params = new JSONObject()
                        .put("room_id", mRoomId)
                        .put("user_id", mUserId)
                        .put("file_uri", uri.toString())
                        .put("file_size", filesize)
                        .put("filename", filename)
                        .put("mime_type", getContext().getContentResolver().getType(uri));
                uriToObserve = MethodCall.create("uploadFile", params).putByContentProvider(getContext());
                c.close();
            }
            else if(ContentResolver.SCHEME_FILE.equals(uri.getScheme())){
                JSONObject params = new JSONObject()
                        .put("room_id", mRoomId)
                        .put("user_id", mUserId)
                        .put("file_uri", uri.toString())
                        .put("file_size", getFileSizeFor(uri))
                        .put("filename", uri.getLastPathSegment())
                        .put("mime_type", MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString())));

                uriToObserve = MethodCall.create("uploadFile", params).putByContentProvider(getContext());
            }
        }
        catch(JSONException e){
            Log.e(Constants.LOG_TAG,"error",e);
        }

        if(uriToObserve!=null) {
            final Uri uri = uriToObserve;

            final ProgressDialog dialog = new ProgressDialog(getContext(), R.style.AppDialog);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setTitle("Uploading file...");
            dialog.setMax(100);
            dialog.show();

            getContext().getContentResolver().registerContentObserver(uri, false, new ContentObserver(null) {
                @Override
                public void onChange(boolean selfChange) {
                    if(getContext()==null) return;

                    Cursor c = getContext().getContentResolver().query(uri,null,null,null,null);
                    if(c!=null && c.moveToFirst()) {
                        MethodCall m = MethodCall.createFromCursor(c);

                        if(m.syncstate==SyncState.SYNCING){
                            try {
                                JSONObject ret = new JSONObject(m.returns);
                                dialog.setProgress((int) (100.0 * ret.getLong("sent") / ret.getLong("total")));
                            }
                            catch (JSONException e){
                                Log.e(Constants.LOG_TAG,"error",e);
                            }
                        }
                        else if (m.syncstate==SyncState.SYNCED){
                            try {
                                JSONObject ret = new JSONObject(m.returns);
                                dialog.setProgress(100);

                                Message msg = new Message();
                                msg.syncstate = SyncState.NOT_SYNCED;
                                msg.content = "File Uploaded: *"+ret.getString("name")+"*\n"+ret.getString("url");
                                msg.roomId = ret.getString("rid");
                                msg.userId = ret.getString("userID");
                                msg.extras = new JSONObject().put("file",new JSONObject().put("_id",ret.getString("_id"))).toString();
                                msg.putByContentProvider(getContext());

                            }
                            catch (JSONException e){
                                Log.e(Constants.LOG_TAG,"error",e);
                            }
                        }

                        if(m.syncstate==SyncState.SYNCED || m.syncstate==SyncState.FAILED) {
                            dialog.dismiss();
                            m.deleteByContentProvider(getContext());
                            getContext().getContentResolver().unregisterContentObserver(this);
                        }
                    }
                }
            });
        }
    }

    private long getFileSizeFor(Uri uri){
        ParcelFileDescriptor pfd = null;
        try {
            pfd = getContext().getContentResolver().openFileDescriptor(uri, "r");
            return Math.max(pfd.getStatSize(), 0);
        } catch (final FileNotFoundException e) {
            Log.e(Constants.LOG_TAG,"error",e);
        } finally {
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (final IOException e) {
                    // Do nothing.
                }
            }
        }
        return -1;
    }
}
