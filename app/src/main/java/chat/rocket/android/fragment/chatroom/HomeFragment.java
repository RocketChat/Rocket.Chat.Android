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
    setTitleText(R.string.home_fragment_title);
  }

  @Override
  public void onResume() {
    super.onResume();
    setTitleDrawableLeft(0);
    setTitleText(R.string.home_fragment_title);
  }
}
