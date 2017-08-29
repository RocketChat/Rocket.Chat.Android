package chat.rocket.android.widget.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import chat.rocket.android.widget.R;
import chat.rocket.android.widget.helper.DrawableHelper;

/**
 * Room list-item view used in sidebar.
 */
public class RoomListItemView extends FrameLayout {
  private String roomId;
  private ImageView roomTypeImage;
  private ImageView userStatusImage;
  private TextView roomNameText;
  private TextView alertCountText;
  private Drawable privateChannelDrawable;
  private Drawable publicChannelDrawable;
  private Drawable userStatusDrawable;

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
    setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    TypedArray array2 = context.getTheme().obtainStyledAttributes(new int[]{
        R.attr.selectableItemBackground
    });
    setBackground(array2.getDrawable(0));
    array2.recycle();

    View.inflate(context, R.layout.room_list_item, this);

    roomTypeImage = findViewById(R.id.image_room_type);
    userStatusImage = findViewById(R.id.image_user_status);
    roomNameText = findViewById(R.id.text_room_name);
    alertCountText = findViewById(R.id.text_alert_count);

    privateChannelDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_lock_white_24dp, null);
    publicChannelDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_hashtag_white_24dp, null);
    userStatusDrawable = VectorDrawableCompat.create(getResources(), R.drawable.ic_user_status_black_24dp, null);
  }

  public String getRoomId() {
    return roomId;
  }

  public RoomListItemView setRoomId(String roomId) {
    this.roomId = roomId;
    return this;
  }

  public RoomListItemView setUnreadCount(int count) {
    if (count > 0) {
      alertCountText.setText(String.valueOf(count));
      alertCountText.setVisibility(View.VISIBLE);
    } else {
      alertCountText.setVisibility(View.GONE);
    }
    return this;
  }

  public RoomListItemView setAlert(boolean alert) {
    setAlpha(alert ? 1.0f : 0.62f);
    return this;
  }

  public String getRoomName() {
    return roomNameText.toString();
  }

  public RoomListItemView setRoomName(String roomName) {
    roomNameText.setText(roomName);
    return this;
  }

  public RoomListItemView showPrivateChannelIcon() {
    roomTypeImage.setImageDrawable(privateChannelDrawable);
    roomTypeImage.setVisibility(VISIBLE);
    return this;
  }

  public RoomListItemView showPublicChannelIcon() {
    roomTypeImage.setImageDrawable(publicChannelDrawable);
    roomTypeImage.setVisibility(VISIBLE);
    return this;
  }

  public RoomListItemView showOnlineUserStatusIcon() {
    prepareDrawableAndShow(R.color.color_user_status_online);
    return this;
  }

  public RoomListItemView showBusyUserStatusIcon() {
    prepareDrawableAndShow(R.color.color_user_status_busy);
    return this;
  }
  public RoomListItemView showAwayUserStatusIcon() {
    prepareDrawableAndShow(R.color.color_user_status_away);
    return this;
  }
  public RoomListItemView showOfflineUserStatusIcon() {
    prepareDrawableAndShow(R.color.color_user_status_offline);
    return this;
  }

  private void prepareDrawableAndShow(int colorResId) {
    DrawableHelper.INSTANCE.wrapDrawable(userStatusDrawable);
    DrawableHelper.INSTANCE.tintDrawable(userStatusDrawable, getContext(), colorResId);
    userStatusImage.setImageDrawable(userStatusDrawable);
    userStatusImage.setVisibility(VISIBLE);
  }
}