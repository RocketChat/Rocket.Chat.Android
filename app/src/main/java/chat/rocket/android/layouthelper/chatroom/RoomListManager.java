package chat.rocket.android.layouthelper.chatroom;

import android.view.View;
import android.view.ViewGroup;

import chat.rocket.android.helper.TextUtils;
import chat.rocket.core.models.Room;
import chat.rocket.android.widget.internal.RoomListItemView;
import java.util.List;

/**
 * Utility class for mapping Room list into channel list ViewGroup.
 */
public class RoomListManager {
  private View unreadTitle;
  private ViewGroup unreadRoomsContainer;
  private ViewGroup channelsContainer;
  private ViewGroup dmContainer;

  private boolean unreadRoomMode = false;

  private List<Room> roomList;

  /**
   * Callback interface for List item clicked.
   */
  public interface OnItemClickListener {
    void onItemClick(RoomListItemView roomListItemView);
  }

  private OnItemClickListener listener;

  /**
   * constructor with three ViewGroups.
   */
  public RoomListManager(View unreadTitle, ViewGroup unreadRoomsContainer,
                         ViewGroup channelsContainer, ViewGroup dmContainer) {
    this(unreadTitle, unreadRoomsContainer, channelsContainer, dmContainer, false);
  }

  /**
   * constructor with two ViewGroups.
   */
  public RoomListManager(View unreadTitle, ViewGroup unreadRoomsContainer,
                         ViewGroup channelsContainer, ViewGroup dmContainer,
                         boolean unreadRoomMode) {
    this.unreadTitle = unreadTitle;
    this.unreadRoomsContainer = unreadRoomsContainer;
    this.channelsContainer = channelsContainer;
    this.dmContainer = dmContainer;
    this.unreadRoomMode = unreadRoomMode;
  }

  /**
   * update ViewGroups with room list.
   */
  public void setRooms(List<Room> roomList) {
    this.roomList = roomList;
    updateRoomsList();
  }

  /**
   * set callback on List item clicked.
   */
  public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
  }

  private void removeDeletedItem(ViewGroup parent, List<Room> roomList) {
    for (int index = parent.getChildCount() - 1; index >= 0; index--) {
      RoomListItemView roomListItemView = (RoomListItemView) parent.getChildAt(index);
      final String targetRoomName = roomListItemView.getRoomName();
      if (!TextUtils.isEmpty(targetRoomName)) {
        boolean found = false;
        for (Room room : roomList) {
          if (targetRoomName.equals(room.getName())) {
            found = true;
            break;
          }
        }
        if (!found) {
          parent.removeViewAt(index);
        }
      }
    }
  }

  public void setUnreadRoomMode(boolean unreadRoomMode) {
    this.unreadRoomMode = unreadRoomMode;
    updateRoomsList();
  }

  private void insertOrUpdateItem(ViewGroup parent, Room room) {
    final String roomName = room.getName();

    int index;
    for (index = 0; index < parent.getChildCount(); index++) {
      RoomListItemView roomListItemView = (RoomListItemView) parent.getChildAt(index);
      final String targetRoomName = roomListItemView.getRoomName();
      if (roomName.equals(targetRoomName)) {
        updateRoomItemView(roomListItemView, room);
        return;
      }
      if (roomName.compareToIgnoreCase(targetRoomName) < 0) {
        break;
      }
    }

    RoomListItemView roomListItemView = new RoomListItemView(parent.getContext());
    updateRoomItemView(roomListItemView, room);
    if (index == parent.getChildCount()) {
      parent.addView(roomListItemView);
    } else {
      parent.addView(roomListItemView, index);
    }
  }

  private void updateRoomItemView(RoomListItemView roomListItemView,
                                  Room room) {
    roomListItemView
        .setRoomId(room.getRoomId())
        .setRoomName(room.getName())
        .setRoomType(room.getType())
        .setAlert(room.isAlert())
        .setUnreadCount(room.getUnread());

    roomListItemView.setOnClickListener(this::onItemClick);
  }

  private void onItemClick(View view) {
    if (view instanceof RoomListItemView && listener != null) {
      listener.onItemClick((RoomListItemView) view);
    }
  }

  private void updateRoomsList() {
    if (roomList == null) {
      return;
    }

    removeDeletedItem(unreadRoomsContainer, roomList);
    removeDeletedItem(channelsContainer, roomList);
    removeDeletedItem(dmContainer, roomList);

    for (Room room : roomList) {
      String name = room.getName();
      if (TextUtils.isEmpty(name)) {
        continue;
      }

      String type = room.getType();

      if (unreadRoomMode && room.isAlert()) {
        insertOrUpdateItem(unreadRoomsContainer, room);
        removeItemIfExists(channelsContainer, name);
        removeItemIfExists(dmContainer, name);
      } else if (Room.TYPE_CHANNEL.equals(type)
          || Room.TYPE_PRIVATE.equals(type)) {
        removeItemIfExists(unreadRoomsContainer, name);
        insertOrUpdateItem(channelsContainer, room);
        removeItemIfExists(dmContainer, name);
      } else if (Room.TYPE_DIRECT_MESSAGE.equals(type)) {
        removeItemIfExists(unreadRoomsContainer, name);
        removeItemIfExists(channelsContainer, name);
        insertOrUpdateItem(dmContainer, room);
      }
    }

    boolean showUnread = unreadRoomMode && unreadRoomsContainer.getChildCount() != 0;
    unreadTitle.setVisibility(showUnread ? View.VISIBLE : View.GONE);
    unreadRoomsContainer.setVisibility(showUnread ? View.VISIBLE : View.GONE);
  }

  private static void removeItemIfExists(ViewGroup parent, String roomName) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      RoomListItemView roomListItemView = (RoomListItemView) parent.getChildAt(i);
      if (roomName.equals(roomListItemView.getRoomName())) {
        parent.removeViewAt(i);
        break;
      }
    }
  }
}
