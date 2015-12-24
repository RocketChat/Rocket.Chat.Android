package chat.rocket.android.fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.User;
import chat.rocket.android.model.UserRoom;
import chat.rocket.android.view.Avatar;

public class MemberListFragment extends AbstractRoomFragment {
    public MemberListFragment(){}

    private static final int LOADER_ID=12332;

    CursorAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().restartLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri = RocketChatProvider.getUriForQuery(UserRoom.TABLE_NAME);
                return new CursorLoader(getContext(), uri, null, "room_id = ? AND EXISTS (SELECT id FROM user WHERE name=username)", new String[]{mRoomId}, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if(mAdapter!=null) mAdapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                if(mAdapter!=null) mAdapter.swapCursor(null);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.member_list_screen, container,false);

        initializeToolbar();
        initializeListView();

        return mRootView;
    }

    private void initializeListView(){
        ListView listView = (ListView) mRootView.findViewById(R.id.listview_members);
        mAdapter = new UserAdapter(getContext(), null);
        listView.setAdapter(mAdapter);
    }

    private static class UserViewHolder {
        public final View itemView;
        public ImageView userStatus;
        public Avatar avatar;
        public TextView username;

        public UserViewHolder(View root, String host) {
            itemView = root;
            userStatus = (ImageView) root.findViewById(R.id.listitem_userstatus_icon);
            avatar = new Avatar(host, itemView.findViewById(R.id.avatar_color), (TextView)itemView.findViewById(R.id.avatar_initials), (ImageView)itemView.findViewById(R.id.avatar_img));
            username = (TextView) root.findViewById(R.id.listitem_username);
        }
    }

    private class UserAdapter extends CursorAdapter {

        LayoutInflater mInflater;

        public UserAdapter(Context context, Cursor cursor) {
            super(context, cursor, true);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.listitem_member, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final UserRoom userRoom = UserRoom.createFromCursor(cursor);
            if(userRoom==null) return;

            User u = RocketChatDatabaseHelper.read(context, new RocketChatDatabaseHelper.DBCallback<User>() {
                @Override
                public User process(SQLiteDatabase db) throws Exception {
                    Log.d("hoge","username="+userRoom.username+", roomID="+userRoom.roomID);
                    return User.getByName(db, userRoom.username);
                }
            });
            if(u==null) return;

            UserViewHolder viewHolder = new UserViewHolder(view, mHost);
            viewHolder.avatar.setForUser(u.name);
            viewHolder.username.setText(u.name);
            viewHolder.userStatus.setImageResource(u.status.getDrawable());
        }
    }

}
