package chat.rocket.android.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import chat.rocket.android.Constants;
import chat.rocket.android.R;
import chat.rocket.android.model.Room;
import ollie.query.Select;

public class MainActivity extends AbstractActivity {
    private static final String TAG = Constants.LOG_TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sliding_pane);

        initializeChannelListView();
    }

    private void initializeChannelListView(){
        ArrayAdapter<Room> adapter = new ArrayAdapter<Room>(this,
                R.layout.listitem_channel,
                R.id.list_item_channel_name,
                Select.from(Room.class).fetch());

        final RecyclerView channelListView = (RecyclerView) findViewById(R.id.listview_channels);
        channelListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        channelListView.setAdapter(new RoomAdapter(this, Select.from(Room.class).fetch()));
    }

    private static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView channelName;

        public RoomViewHolder(View itemView) {
            super(itemView);
            channelName = (TextView) itemView.findViewById(R.id.list_item_channel_name);
        }
    }

    private static class RoomAdapter extends RecyclerView.Adapter<RoomViewHolder> {

        private LayoutInflater mInflater;
        private List<Room> mRooms;

        public RoomAdapter(Context context, List<Room> data) {
            mInflater = LayoutInflater.from(context);
            mRooms = data;
        }

        @Override
        public RoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RoomViewHolder(mInflater.inflate(R.layout.listitem_channel, parent, false));
        }

        @Override
        public void onBindViewHolder(RoomViewHolder holder, int position) {
            holder.channelName.setText(mRooms.get(position).name);
        }

        @Override
        public int getItemCount() {
            return mRooms.size();
        }
    }
}
