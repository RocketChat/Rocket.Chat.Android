package chat.rocket.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.Constants;
import chat.rocket.android.R;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.fragment.AbstractRoomFragment;
import chat.rocket.android.fragment.AddRoomDialogFragment;
import chat.rocket.android.fragment.ChatRoomFragment;
import chat.rocket.android.fragment.HomeRoomFragment;
import chat.rocket.android.model.Message;
import chat.rocket.android.model2.MethodCall;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.User;
import chat.rocket.android.preference.Cache;
import chat.rocket.android.view.Avatar;
import chat.rocket.android.view.CursorRecyclerViewAdapter;
import io.realm.Realm;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

public class MainActivity extends AbstractActivity {
    private static final String TAG = Constants.LOG_TAG;
    private static final int LOADER_ID = 0x12345;
    public static final String TOGGLE_NAV_ACTION = MainActivity.class.getName()+".intent.action.TOGGLE_NAV";

    private RoomAdapter mAdapter;

    @Override
    protected int getContainerId() {
        return R.id.activity_main_container;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        requestGetRooms();
        showHomeRoomFragment();

        setupUserInfo();
        setupUserActionToggle();
        loadRooms();
        setupAddRoomButton();
        openPaneIfNeededForInitialLayout();
        LocalBroadcastManager.getInstance(this).registerReceiver(mToggleNavReveiver, new IntentFilter(TOGGLE_NAV_ACTION));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mToggleNavReveiver);
        super.onDestroy();
    }

    private ServerConfig getPrimaryServerConfig() {
        return RocketChatDatabaseHelper.read(this, new RocketChatDatabaseHelper.DBCallback<ServerConfig>() {
            @Override
            public ServerConfig process(SQLiteDatabase db) {
                return ServerConfig.getPrimaryConfig(db);
            }
        });
    }

    private void requestGetRooms() {
        try {
            MethodCall.create("rooms/get", new JSONObject().put("timestamp", 0));
        } catch (JSONException e) {
        }
    }

    private BroadcastReceiver mToggleNavReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(TOGGLE_NAV_ACTION.equals(intent.getAction())) {
                togglePaneIfNeeded();
            }
        }
    };

    private Handler mHandler = new Handler();
    private void setupUserInfo(){
        final ServerConfig s = getPrimaryServerConfig();
        final String userId = Cache.get(MainActivity.this).getString(Cache.KEY_MY_USER_ID,"");
        final String username = Cache.get(this).getString(Cache.KEY_MY_USER_NAME,"");
        setupUserStatus(userId);
        setupUserInfoInner(s, username);
        Cache.waitForValue(this, Cache.KEY_MY_USER_NAME, new Cache.ValueCallback<String>() {
            @Override
            public void onGetValue(String value) {
                setupUserInfoInner(s, value);
            }
        });
        Cache.waitForValue(this, Cache.KEY_MY_USER_ID, new Cache.ValueCallback<String>() {
            @Override
            public void onGetValue(String value) {
                setupUserStatus(value);
            }
        });
    }

    private void setupUserInfoInner(ServerConfig s, final String username){
        if(s!=null){
            ((TextView) findViewById(R.id.txt_hostname_info)).setText(s.hostname);
            if(!TextUtils.isEmpty(username)) {
                ((TextView) findViewById(R.id.txt_account_info)).setText(username);
                new Avatar(s.hostname,
                        findViewById(R.id.avatar_color),
                        (TextView)findViewById(R.id.avatar_initials),
                        (ImageView)findViewById(R.id.avatar_img)).setForUser(username);
            }
            else ((TextView) findViewById(R.id.txt_account_info)).setText(s.account);
        }
        else {
            ((TextView) findViewById(R.id.txt_hostname_info)).setText("--");
            ((TextView) findViewById(R.id.txt_account_info)).setText("---");
        }
    }

    private void setupUserStatus(final String userId) {
        if(TextUtils.isEmpty(userId)) return;

        User u = RocketChatDatabaseHelper.read(this, new RocketChatDatabaseHelper.DBCallback<User>() {
            @Override
            public User process(SQLiteDatabase db) throws Exception {
                return User.getById(db,userId);
            }
        });
        getContentResolver().unregisterContentObserver(mUserObserver);
        if(u!=null) {
            setupUserStatusInner(userId);
            getContentResolver().registerContentObserver(RocketChatProvider.getUriForQuery(User.TABLE_NAME,u._id), false, mUserObserver);
        }
    }
    private ContentObserver mUserObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            final String userId = Cache.get(MainActivity.this).getString(Cache.KEY_MY_USER_ID,"");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setupUserStatusInner(userId);
                }
            });
        }
    };

    private void setupUserStatusInner(final String userId) {
        if(TextUtils.isEmpty(userId)) return;

        User u = RocketChatDatabaseHelper.read(MainActivity.this, new RocketChatDatabaseHelper.DBCallback<User>() {
            @Override
            public User process(SQLiteDatabase db) throws Exception {
                return User.getById(db,userId);
            }
        });
        if(u!=null) ((ImageView) findViewById(R.id.img_userstatus)).setImageResource(u.status.getDrawable());
        else ((ImageView) findViewById(R.id.img_userstatus)).setImageResource(User.Status.OFFLINE.getDrawable());
    }


    private User.Status[] mUserStatusItems = new User.Status[]{
            User.Status.ONLINE,
            User.Status.AWAY,
            User.Status.BUSY,
            User.Status.OFFLINE
    };
    private AdapterView.OnItemClickListener mUserStatusItemCallbacks = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Context context = parent.getContext();
            if(position<0 || position>= mUserStatusItems.length) return;

            final String userId = Cache.get(MainActivity.this).getString(Cache.KEY_MY_USER_ID,"");
            User u = RocketChatDatabaseHelper.read(view.getContext(), new RocketChatDatabaseHelper.DBCallback<User>() {
                @Override
                public User process(SQLiteDatabase db) throws Exception {
                    return User.getById(db, userId);
                }
            });

            if(u==null) return;

            User.Status status = mUserStatusItems[position];

            u.status = status;
            u.syncstate = SyncState.NOT_SYNCED;
            u.putByContentProvider(view.getContext());
            toggleUserActionView();
        }
    };

    private String[] mUserActionItems = new String[]{
            "Logout"
    };
    private AdapterView.OnItemClickListener mUserActionItemCallbacks = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Context context = parent.getContext();
            final ServerConfig s = getPrimaryServerConfig();
            if(s!=null) {
                MethodCall.create("logout",null);
                RocketChatDatabaseHelper.writeWithTransaction(context, new RocketChatDatabaseHelper.DBCallbackEx<Object>() {
                    @Override
                    public Object process(SQLiteDatabase db) throws Exception {
                        Room.delete(db, null,null);
                        Message.delete(db, null,null);
                        User.delete(db, null,null);
                        s.delete(db);

                        return null;
                    }

                    @Override
                    public void handleException(Exception e) {
                        Log.e(TAG, "error", e);
                    }
                });
                Cache.get(context).edit()
                        .remove(Cache.KEY_MY_USER_ID)
                        .remove(Cache.KEY_MY_USER_NAME)
                        .commit();
                RealmHelperBolts.executeTransactionAsync(new RealmHelperBolts.Transaction() {
                    @Override
                    public Object execute(Realm realm) throws Exception {
                        realm.deleteAll();
                        return null;
                    }
                });
                showEntryActivity();
            }
        }
    };


    private void showEntryActivity(){
        Intent intent = new Intent(this, EntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setupUserActionToggle(){
        final ListView userStatusActionList = (ListView) findViewById(R.id.listview_user_status_actions);
        userStatusActionList.setAdapter(new ArrayAdapter<User.Status>(this, R.layout.listitem_navi_menu, R.id.listitem_userstatus_text, mUserStatusItems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View listitem = super.getView(position, convertView, parent);
                if (position>=0 && position< mUserStatusItems.length) {
                    ((ImageView) listitem.findViewById(R.id.listitem_userstatus_icon)).setImageResource(mUserStatusItems[position].getDrawable());
                }
                return listitem;
            }
        });
        userStatusActionList.setOnItemClickListener(mUserStatusItemCallbacks);

        final ListView userActionList = (ListView) findViewById(R.id.listview_user_actions);
        userActionList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mUserActionItems));
        userActionList.setOnItemClickListener(mUserActionItemCallbacks);

        findViewById(R.id.user_info_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleUserActionView();
            }
        });
    }

    private void toggleUserActionView(){
        final ListView userStatusActionList = (ListView) findViewById(R.id.listview_user_status_actions);
        final ListView userActionList = (ListView) findViewById(R.id.listview_user_actions);
        final View toggle = findViewById(R.id.img_user_action_toggle);
        if(userActionList.getVisibility()==View.GONE) {
            toggle.animate()
                    .rotation(180)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            userStatusActionList.setVisibility(View.VISIBLE);
                            userActionList.setVisibility(View.VISIBLE);
                        }
                    }).start();
        }
        else {
            toggle.animate()
                    .rotation(0)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            userStatusActionList.setVisibility(View.GONE);
                            userActionList.setVisibility(View.GONE);
                        }
                    }).start();
        }
    }

    private void loadRooms(){
        mAdapter = new RoomAdapter(this, null);
        final RecyclerView roomListView = (RecyclerView) findViewById(R.id.listview_rooms);
        roomListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        roomListView.setAdapter(mAdapter);

        getSupportLoaderManager().restartLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri = RocketChatProvider.getUriForQuery(Room.TABLE_NAME);
                return new CursorLoader(MainActivity.this, uri, null, "syncstate=2", null, null);
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

    private void setupAddRoomButton(){
        final FloatingActionMenu fabMenu = (FloatingActionMenu) findViewById(R.id.fab_menu_add_room);

        findViewById(R.id.btn_add_channel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);
                AddRoomDialogFragment.create(Room.Type.CHANNEL).show(getSupportFragmentManager(), "add-room");
            }
        });
        findViewById(R.id.btn_add_direct_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);
                AddRoomDialogFragment.create(Room.Type.DIRECT_MESSAGE).show(getSupportFragmentManager(), "add-room");
            }
        });
        findViewById(R.id.btn_add_private_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);
                AddRoomDialogFragment.create(Room.Type.PRIVATE_GROUP).show(getSupportFragmentManager(), "add-room");
            }
        });
    }

    private void openPaneIfNeededForInitialLayout() {
        // pane.isOpen is not correct before OnLayout.
        // https://code.google.com/p/android/issues/detail?id=176340
        final SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
        if(pane!=null) pane.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                pane.removeOnLayoutChangeListener(this);
                pane.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openPaneIfNeeded();
                    }
                }, 500);
            }
        });
    }

    private void openPaneIfNeeded() {
        final SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
        if (pane!=null && pane.isSlideable() && !pane.isOpen()) {
            pane.openPane();
        }
    }

    private void closePaneIfNeeded(){
        final SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
        if (pane!=null && pane.isSlideable() && pane.isOpen()) {
            pane.closePane();
        }
    }

    private void togglePaneIfNeeded(){
        final SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
        if (pane!=null && pane.isSlideable() && pane.isOpen()) {
            pane.closePane();
        }
        else if (pane!=null && pane.isSlideable() && !pane.isOpen()) {
            pane.openPane();
        }
    }


    private static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView roomType;
        TextView roomName;
        View unreadContainer;
        TextView unreadCount;

        public RoomViewHolder(View itemView) {
            super(itemView);
            roomType = (ImageView) itemView.findViewById(R.id.list_item_room_type);
            roomName = (TextView) itemView.findViewById(R.id.list_item_room_name);
            unreadContainer = itemView.findViewById(R.id.list_item_room_unread_container);
            unreadCount = (TextView) itemView.findViewById(R.id.list_item_room_unread_count);
        }
    }

    private class RoomAdapter extends CursorRecyclerViewAdapter<RoomViewHolder> {

        LayoutInflater mInflater;

        public RoomAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RoomViewHolder(mInflater.inflate(R.layout.listitem_room, parent, false));
        }

        @Override
        public void bindView(RoomViewHolder viewHolder, Context context, int position, Cursor cursor) {
            final Room r = Room.createFromCursor(cursor);

            switch (r.type){
                case CHANNEL:
                    viewHolder.roomType.setImageResource(R.drawable.ic_room_type_channel);
                    break;
                case PRIVATE_GROUP:
                    viewHolder.roomType.setImageResource(R.drawable.ic_room_type_private);
                    break;
                case DIRECT_MESSAGE:
                    viewHolder.roomType.setImageResource(R.drawable.ic_room_type_direct);
                    break;
            }

            viewHolder.roomName.setText(r.name);
            viewHolder.roomName.setTypeface(null, r.alert? Typeface.BOLD : Typeface.NORMAL);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closePaneIfNeeded();
                    showChatRoomFragment(r);
                    if(r.alert) {
                        r.alert = false;
                        r.syncstate = SyncState.NOT_SYNCED;
                        r.putByContentProvider(v.getContext());
                    }
                }
            });
            viewHolder.unreadContainer.setVisibility(r.unread>0 ? View.VISIBLE : View.GONE);
            viewHolder.unreadCount.setText(Integer.toString(r.unread));
        }
    }

    private void showHomeRoomFragment(){
        getSupportFragmentManager().beginTransaction()
                .replace(getContainerId(), new HomeRoomFragment())
                .commit();
    }

    private void showChatRoomFragment(Room r) {
        ServerConfig s = getPrimaryServerConfig();
        if(s!=null) {
            if (getSupportFragmentManager().findFragmentById(getContainerId()) instanceof AbstractRoomFragment) {
                getSupportFragmentManager().popBackStack();
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(getContainerId(), ChatRoomFragment.create(s.hostname, s.authToken, r))
                    .commit();
        }
    }
}
