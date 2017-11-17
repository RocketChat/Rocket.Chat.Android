package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;

import java.util.List;

import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomSidebar;

public class ChannelRoomListHeader implements RoomListHeader {

  private final String title;
  private final ClickListener clickListener;

  public ChannelRoomListHeader(String title, ClickListener clickListener) {
    this.title = title;
    this.clickListener = clickListener;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean owns(RoomSidebar roomSidebar) {
    return roomSidebar.getType().equals(Room.TYPE_CHANNEL) || roomSidebar.getType().equals(Room.TYPE_GROUP);
  }

  @Override
  public boolean shouldShow(@NonNull List<RoomSidebar> roomSidebarList) {
    for (RoomSidebar roomSidebar: roomSidebarList) {
      if ((roomSidebar.getType().equals(Room.TYPE_CHANNEL)
              || roomSidebar.getType().equals(Room.TYPE_GROUP))
              && !roomSidebar.isAlert()
              && !roomSidebar.isFavorite()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ClickListener getClickListener() {
    return clickListener;
  }
}