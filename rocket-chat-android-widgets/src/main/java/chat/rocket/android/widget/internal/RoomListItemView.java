package chat.rocket.android.widget.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import chat.rocket.android.widget.R;
import java.util.HashMap;

/**
 * Room list-item view used in sidebar.
 */
public class RoomListItemView extends LinearLayout {
  private String roomId;
  private String roomName;

  private static HashMap<String, Integer> ICON_TABLE = new HashMap<String, Integer>(){
    {
      put("c", R.string.fa_hashtag);
      put("p", R.string.fa_lock);
      put("d", R.string.fa_at);
    }
  };

  public RoomListItemView(Context context) {
    super(context);
    initialize(context);
  }

  public RoomListItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  public RoomListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RoomListItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context);
  }

  private void initialize(Context context) {
    setOrientation(HORIZONTAL);

    TypedArray array2 = context.getTheme().obtainStyledAttributes(new int[]{
        R.attr.selectableItemBackground
    });
    setBackground(array2.getDrawable(0));
    array2.recycle();

    View.inflate(context, R.layout.room_list_item, this);
  }

  public String getRoomId() {
    return roomId;
  }

  public RoomListItemView setRoomId(String roomId) {
    this.roomId = roomId;
    return this;
  }

  public RoomListItemView setRoomType(String type) {
    if (ICON_TABLE.containsKey(type)) {
      TextView icon = (TextView) findViewById(R.id.icon);
      icon.setText(ICON_TABLE.get(type));
    }

    return this;
  }

  public RoomListItemView setAlertCount(int count) {
    View alertCountContainer = findViewById(R.id.alert_count_container);
    TextView alertCount = (TextView) findViewById(R.id.alert_count);
    if (count > 0) {
      alertCount.setText(Integer.toString(count));
      alertCountContainer.setVisibility(View.VISIBLE);
    } else {
      alertCountContainer.setVisibility(View.GONE);
    }

    return this;
  }

  public String getRoomName() {
    return roomName;
  }

  public RoomListItemView setRoomName(String roomName) {
    this.roomName = roomName;
    TextView text = (TextView) findViewById(R.id.text);
    text.setText(roomName);

    return this;
  }
}
