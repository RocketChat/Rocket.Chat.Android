package chat.rocket.android.fragment.chatroom;

import chat.rocket.android.R;

public class HomeFragment extends AbstractChatRoomFragment {
  public HomeFragment() {
  }

  @Override protected int getLayout() {
    return R.layout.fragment_home;
  }

  @Override protected void onSetupView() {
    activityToolbar.setTitle(R.string.home_fragment_title);
  }

  @Override public void onResume() {
    super.onResume();
    activityToolbar.setNavigationIcon(null);
    activityToolbar.setTitle(R.string.home_fragment_title);
  }
}
