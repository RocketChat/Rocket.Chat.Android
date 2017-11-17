package chat.rocket.android.layouthelper.chatroom.roomlist;

import android.support.annotation.NonNull;

import java.util.List;

import chat.rocket.core.models.RoomSidebar;

public interface RoomListHeader {

  String getTitle();

  boolean owns(RoomSidebar roomSidebar);

  boolean shouldShow(@NonNull List<RoomSidebar> roomSidebarList);

  ClickListener getClickListener();

  interface ClickListener {
    void onClick();
  }
}
