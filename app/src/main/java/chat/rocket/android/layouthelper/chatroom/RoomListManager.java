package chat.rocket.android.layouthelper.chatroom;

import android.view.ViewGroup;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.Room;
import chat.rocket.android.widget.internal.RoomListItemView;
import java.util.List;

/**
 * Utility class for mapping Room list into channel list ViewGroup.
 */
public class RoomListManager {
  private ViewGroup channelsContainer;
  private ViewGroup dmContainer;

  /**
   * constructor with two ViewGroups.
   */
  public RoomListManager(ViewGroup channelsContainer, ViewGroup dmContainer) {
    this.channelsContainer = channelsContainer;
    this.dmContainer = dmContainer;
  }

  public void setRooms(List<Room> roomList) {
    for (Room room : roomList) {
      String name = room.getName();
      if (TextUtils.isEmpty(name)) {
        continue;
      }

      String type = room.getT();

      if (Room.TYPE_CHANNEL.equals(type) || Room.TYPE_PRIVATE.equals(type)) {
        insertOrUpdateItem(channelsContainer, room);
        removeItemIfExists(dmContainer, name);
      } else if (Room.TYPE_DIRECT_MESSAGE.equals(type)) {
        removeItemIfExists(channelsContainer, name);
        insertOrUpdateItem(dmContainer, room);
      }
    }
  }

  private static void insertOrUpdateItem(ViewGroup parent, Room room) {
    final String roomName = room.getName();

    int index;
    for (index = 0; index < parent.getChildCount(); index++) {
      RoomListItemView roomListItemView = (RoomListItemView) parent.getChildAt(index);
      final String targetRoomName = roomListItemView.getName();
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

  private static void updateRoomItemView(RoomListItemView roomListItemView, Room room) {
    roomListItemView
        .setRoom(room.getT(), room.getName())
        .setAlertCount(0); // TODO not implemented yet.
  }

  private static void removeItemIfExists(ViewGroup parent, String roomName) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      RoomListItemView roomListItemView = (RoomListItemView) parent.getChildAt(i);
      if (roomName.equals(roomListItemView.getName())) {
        parent.removeViewAt(i);
        break;
      }
    }
  }
}
