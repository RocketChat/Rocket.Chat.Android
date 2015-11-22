package chat.rocket.android.activity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import chat.rocket.android.Constants;
import chat.rocket.android.R;
import chat.rocket.android.view.CursorRecyclerViewAdapter;

public class MainActivity extends AbstractActivity {
    private static final String TAG = Constants.LOG_TAG;
    private static final int LOADER_ID = 0x12345;

    private RoomAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sliding_pane);

        loadChannel();
    }

    private void loadChannel(){
        mAdapter = new RoomAdapter(this, null);
        final RecyclerView channelListView = (RecyclerView) findViewById(R.id.listview_channels);
        channelListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        channelListView.setAdapter(mAdapter);

        getSupportLoaderManager().restartLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri uri = Uri.parse("content://chat.rocket.android/room");
                return new CursorLoader(MainActivity.this, uri, null,null,null,null);
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

    private static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView channelName;

        public RoomViewHolder(View itemView) {
            super(itemView);
            channelName = (TextView) itemView.findViewById(R.id.list_item_channel_name);
        }
    }

    private static class RoomAdapter extends CursorRecyclerViewAdapter<RoomViewHolder> {

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
            return new RoomViewHolder(mInflater.inflate(R.layout.listitem_channel, parent, false));
        }

        @Override
        public void bindView(RoomViewHolder viewHolder, Context context, Cursor cursor) {
            viewHolder.channelName.setText(cursor.getString(cursor.getColumnIndex("name")));
        }
    }
}
