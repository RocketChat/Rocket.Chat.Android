package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import chat.rocket.android.R;
import chat.rocket.android.fragment.chatroom.HomeFragment;
import chat.rocket.android.helper.Avatar;

/**
 * Entry-point for Rocket.Chat.Android application.
 */
public class MainActivity extends AbstractAuthedActivity {
  @Override protected int getLayoutContainerForFragment() {
    return R.id.activity_main_container;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      showFragment(new HomeFragment());
    }

    ImageView myAvatar = (ImageView) findViewById(R.id.img_my_avatar);
    new Avatar("demo.rocket.chat", "John Doe").into(myAvatar);
  }
}
