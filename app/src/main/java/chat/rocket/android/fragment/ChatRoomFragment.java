package chat.rocket.android.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.github.clans.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

import chat.rocket.android.Constants;
import chat.rocket.android.R;
import chat.rocket.android.activity.MainActivity;
import chat.rocket.android.activity.OnBackPressListener;
import chat.rocket.android.api.ws.RocketChatWSService;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model2.MethodCall;
import chat.rocket.android.model2.SyncState;
import chat.rocket.android.preference.Cache;
import chat.rocket.android.view.LoadMoreScrollListener;
import chat.rocket.android.view.MessageComposer;
import io.realm.Realm;
import io.realm.RealmQuery;
import jp.co.crowdworks.realm_java_helpers.RealmObjectObserver;

public class ChatRoomFragment extends AbstractRoomFragment implements OnBackPressListener, FragmentManager.OnBackStackChangedListener{
    private static final int LOADER_ID = 0x12346;
    private static final int PICK_IMAGE_ID = 0x11;

    public ChatRoomFragment(){}

    private String mToken;
    private boolean mRoomHasMore;
    private String mUserId;
    private String mUsername;
    private MessageAdapter mAdapter;
    private LoadMoreScrollListener mLoadMoreListener;

    public static ChatRoomFragment create(String host, String token, Room r) {
        ChatRoomFragment f = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString("host", host);
        args.putString("token", token);
        args.putLong("roomBaseId", r._id);
        args.putString("roomId", r.id);
        args.putString("roomName", r.name);
        args.putString("roomType", r.type.getValue());
        args.putBoolean("roomHasMore", r.hasMore);
        f.setArguments(args);
        return f;
    }

    @Override
    protected void initFromArgs(Bundle args) {
        super.initFromArgs(args);
        mToken = args.getString("token");
        mRoomHasMore = args.getBoolean("roomHasMore");
    }

    @Override
    protected boolean hasValidArgs(Bundle args) {
        return super.hasValidArgs(args)
                && args.containsKey("token")
                && args.containsKey("roomHasMore");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    }

    @Override
    protected void onRoomLoaded(final Room r) {
        mRootView.post(new Runnable() {
            @Override
            public void run() {
                if(mRoomHasMore != r.hasMore){
                    mRoomHasMore = r.hasMore;
                    setupListViewHasMore();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.chat_room_screen, container, false);

        RocketChatWSService.keepalive(getContext());
        initializeToolbar();
        setupListView();
        loadMessages();
        fetchNewMessages();
        setupMessageComposer();
        setupUploader();
        return mRootView;
    }

    protected void initializeToolbar() {
        Toolbar bar = (Toolbar) mRootView.findViewById(R.id.toolbar_chatroom);
        setHasOptionsMenu(true);
        getAppCompatActivity().setSupportActionBar(bar);

        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager.getInstance(v.getContext())
                        .sendBroadcast(new Intent(MainActivity.TOGGLE_NAV_ACTION));
            }
        });

        setupToolbar();
    }

    protected void setupToolbar(){
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chat_room_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_room_search:
                showSearchFragment();
                break;
            case R.id.chat_room_show_members:
                showMemberListFragment();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchNewMessages() {
        ServerConfig s = getPrimaryServerConfig();
        if (s == null) return;

        try {
            MethodCall.create("loadMessages", new JSONObject().put("room_id",mRoomId).put("clean",true));
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
            final String id = MethodCall.create("loadMessages", new JSONObject().put("room_id",mRoomId).put("end_ts",m.timestamp));

            new RealmObjectObserver<MethodCall>() {
                @Override
                protected RealmQuery<MethodCall> query(Realm realm) {
                    return realm.where(MethodCall.class).equalTo("id", id);
                }

                @Override
                protected void onChange(MethodCall methodCall) {
                    if(mLoadMoreListener!=null) mLoadMoreListener.setLoadingDone();
                    unsub();
                }
            }.sub();
        } catch (JSONException e) {
            Log.e(Constants.LOG_TAG, "error", e);
        }
    }

    private void setupListView() {
        final Context context = mRootView.getContext();
        mAdapter = new MessageAdapter(context, null, mHost, mUserId, mToken);
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

    @Override
    public void onBackStackChanged() {
        if(getActivity()!=null) {
            initializeToolbar();
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
        m.syncstate = chat.rocket.android.model.SyncState.NOT_SYNCED;
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
            btnUploadFile.hide(true);
            btnCompose.hide(true);
            composer.show(null);
        }
        else{
            composer.hide(new Runnable() {
                @Override
                public void run() {
                    btnCompose.show(true);
                    btnUploadFile.show(true);
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

        String id = null;
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
                id = MethodCall.create("uploadFile", params);
            }
            else if(ContentResolver.SCHEME_FILE.equals(uri.getScheme())){
                JSONObject params = new JSONObject()
                        .put("room_id", mRoomId)
                        .put("user_id", mUserId)
                        .put("file_uri", uri.toString())
                        .put("file_size", getFileSizeFor(uri))
                        .put("filename", uri.getLastPathSegment())
                        .put("mime_type", MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString())));

                id = MethodCall.create("uploadFile", params);
            }
            if (c!=null && !c.isClosed()) c.close();
        }
        catch(JSONException e){
            Log.e(Constants.LOG_TAG,"error",e);
        }

        if(id!=null) {
            final ProgressDialog dialog = new ProgressDialog(getContext(), R.style.AppDialog);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setTitle("Uploading file...");
            dialog.setMax(100);
            dialog.show();

            final String _id = id;

            new RealmObjectObserver<MethodCall>() {
                @Override
                protected RealmQuery<MethodCall> query(Realm realm) {
                    return realm.where(MethodCall.class).equalTo("id", _id);
                }

                @Override
                protected void onChange(MethodCall methodCall) {
                    int syncstate = methodCall.getSyncstate();

                    if(syncstate==SyncState.SYNCING){
                        try {
                            JSONObject ret = new JSONObject(methodCall.getReturns());
                            dialog.setProgress((int) (100.0 * ret.getLong("sent") / ret.getLong("total")));
                        }
                        catch (JSONException e){
                            Log.e(Constants.LOG_TAG,"error",e);
                        }
                    }
                    else if (syncstate==SyncState.SYNCED){
                        try {
                            JSONObject ret = new JSONObject(methodCall.getReturns());
                            dialog.setProgress(100);

                            JSONObject attachment = new JSONObject()
                                    .put("title","File Uploaded: "+ret.getString("name"))
                                    .put("title_url",ret.getString("url"))
                                    .put("image_url",ret.getString("url"))
                                    .put("image_type",ret.getString("type"))
                                    .put("image_size",ret.getLong("size"));

                            Message msg = new Message();
                            msg.syncstate = chat.rocket.android.model.SyncState.NOT_SYNCED;
                            msg.content = "";
                            msg.roomId = ret.getString("rid");
                            msg.userId = ret.getString("userID");
                            msg.extras = new JSONObject().put("file",new JSONObject().put("_id",ret.getString("_id"))).toString();
                            msg.flags &= ~Message.FLAG_GROUPABLE;
                            msg.attachments = new JSONArray().put(attachment).toString();
                            msg.putByContentProvider(getContext());

                        }
                        catch (JSONException e){
                            Log.e(Constants.LOG_TAG,"error",e);
                        }
                    }

                    if(syncstate==SyncState.SYNCED || syncstate==SyncState.FAILED) {
                        dialog.dismiss();
                        unsub();

                        MethodCall.delete(_id);
                    }
                }
            }.sub();
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

    private void showSearchFragment() {
        Fragment f = new SearchMessageFragment();
        f.setArguments(getArguments());
        getFragmentManager().beginTransaction()
                .add(R.id.activity_main_container, f)
                .addToBackStack(null)
                .commit();
    }

    private void showMemberListFragment() {
        Fragment f = new MemberListFragment();
        f.setArguments(getArguments());
        getFragmentManager().beginTransaction()
                .add(R.id.activity_main_container, f)
                .addToBackStack(null)
                .commit();
    }
}
