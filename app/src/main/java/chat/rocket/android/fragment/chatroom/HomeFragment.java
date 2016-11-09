package chat.rocket.android.fragment.chatroom;

import chat.rocket.android.R;

public class HomeFragment extends AbstractChatRoomFragment {
  @Override protected int getLayout() {
    return R.layout.fragment_home;
  }

  @Override protected void onSetupView() {
    activityToolbar.setTitle("Rocket.Chat - Home");
  }
}
