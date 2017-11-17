package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;

import java.util.List;

import chat.rocket.core.models.RoomSidebar;

public class FavoriteRoomListHeader implements RoomListHeader {

  private final String title;

  public FavoriteRoomListHeader(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public boolean owns(RoomSidebar roomSidebar) {
    return roomSidebar.isFavorite();
  }

  @Override
  public boolean shouldShow(@NonNull List<RoomSidebar> roomSidebarList) {
    for (RoomSidebar roomSidebar: roomSidebarList) {
      if (roomSidebar.isFavorite() && !roomSidebar.isAlert()) {
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