package chat.rocket.android.fragment.chatroom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import chat.rocket.android.R;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.widget.RoomToolbar;
import chat.rocket.core.models.User;

abstract class AbstractChatRoomFragment extends AbstractFragment {
  private RoomToolbar roomToolbar;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    roomToolbar = getActivity().findViewById(R.id.activity_main_toolbar);
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  protected void setToolbarTitle(CharSequence title) {
    roomToolbar.setTitle(title);
  }

  protected void showToolbarPrivateChannelIcon() {
    roomToolbar.showPrivateChannelIcon();
  }

  protected void showToolbarPublicChannelIcon() {
    roomToolbar.showPublicChannelIcon();
  }

  protected void showToolbarUserStatuslIcon(@Nullable String status) {
    if (status == null) {
      roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_OFFLINE);
    } else {
      switch (status) {
        case User.STATUS_ONLINE:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_ONLINE);
          break;
        case User.STATUS_BUSY:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_BUSY);
          break;
        case User.STATUS_AWAY:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_AWAY);
          break;
        default:
          roomToolbar.showUserStatusIcon(RoomToolbar.STATUS_OFFLINE);
          break;
      }
    }
  }
}