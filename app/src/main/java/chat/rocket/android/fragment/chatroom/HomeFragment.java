package chat.rocket.android.fragment.chatroom;

import chat.rocket.android.R;

public class HomeFragment extends AbstractChatRoomFragment {
  public HomeFragment() {
  }

  @Override
  protected int getLayout() {
    return R.layout.fragment_home;
  }

  @Override
  protected void onSetupView() {
    setToolbarTitle(R.string.home_fragment_title);
  }

  @Override
  public void onResume() {
    super.onResume();
    setToolbarRoomIcon(0);
    setToolbarTitle(R.string.home_fragment_title);
  }
}
