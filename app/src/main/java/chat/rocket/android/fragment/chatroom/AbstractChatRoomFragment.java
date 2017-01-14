package chat.rocket.android.fragment.chatroom;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import chat.rocket.android.R;
import chat.rocket.android.fragment.AbstractFragment;
import chat.rocket.android.widget.RoomToolbar;

abstract class AbstractChatRoomFragment extends AbstractFragment {

  private RoomToolbar roomToolbar;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    roomToolbar = (RoomToolbar) getActivity().findViewById(R.id.activity_main_toolbar);
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  protected void setToolbarTitle(@StringRes int stringResId) {
    if (roomToolbar == null) {
      return;
    }

    roomToolbar.setTitle(stringResId);
  }

  protected void setToolbarTitle(CharSequence title) {
    if (roomToolbar == null) {
      return;
    }

    roomToolbar.setTitle(title);
  }

  protected void setToolbarRoomIcon(@DrawableRes int drawableResId) {
    if (roomToolbar == null) {
      return;
    }

    roomToolbar.setRoomIcon(drawableResId);
    roomToolbar.setUnreadBudge((int) (System.currentTimeMillis() % 15)); //TODO: just example!
  }
}
