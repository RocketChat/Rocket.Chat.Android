package chat.rocket.android.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import chat.rocket.android.R;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.UserRoom;

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
        mAdapter = new UserAdapter(getContext(), null, mHost);
        listView.setAdapter(mAdapter);
    }
}
