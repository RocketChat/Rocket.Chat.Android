package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;

import java.util.List;

import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomSidebar;

public class LivechatRoomListHeader implements RoomListHeader {

  private final String title;

  public LivechatRoomListHeader(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean owns(RoomSidebar roomSidebar) {
    return Room.TYPE_LIVECHAT.equals(roomSidebar.getType());
  }

  @Override
  public boolean shouldShow(@NonNull List<RoomSidebar> roomSidebarList) {
    for (RoomSidebar roomSidebar: roomSidebarList) {
      if (owns(roomSidebar)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ClickListener getClickListener() {
    return null;
  }
}