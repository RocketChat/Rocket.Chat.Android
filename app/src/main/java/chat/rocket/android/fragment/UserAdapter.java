package chat.rocket.android.fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import chat.rocket.android.R;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.User;
import chat.rocket.android.model.UserRoom;
import chat.rocket.android.view.Avatar;

/*package*/ class UserAdapter extends CursorAdapter {
    private final LayoutInflater mInflater;
    private final String mHost;

    public UserAdapter(Context context, Cursor cursor, String host) {
        super(context, cursor, true);
        mInflater = LayoutInflater.from(context);
        mHost = host;
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

        ViewHolder viewHolder = new ViewHolder(view, mHost);
        viewHolder.avatar.setForUser(u.name);
        viewHolder.username.setText(u.name);
        viewHolder.userStatus.setImageResource(u.status.getDrawable());
    }

    private static class ViewHolder {
        public final View itemView;
        public ImageView userStatus;
        public Avatar avatar;
        public TextView username;

        public ViewHolder(View root, String host) {
            itemView = root;
            userStatus = (ImageView) root.findViewById(R.id.listitem_userstatus_icon);
            avatar = new Avatar(host, itemView.findViewById(R.id.avatar_color), (TextView)itemView.findViewById(R.id.avatar_initials), (ImageView)itemView.findViewById(R.id.avatar_img));
            username = (TextView) root.findViewById(R.id.listitem_username);
        }
    }

}
