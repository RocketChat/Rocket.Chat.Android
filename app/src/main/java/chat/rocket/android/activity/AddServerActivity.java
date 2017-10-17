package chat.rocket.android.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import chat.rocket.android.R;
import chat.rocket.android.fragment.add_server.InputHostnameFragment;

public class AddServerActivity extends AbstractFragmentActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    setContentView(R.layout.simple_screen);
    showFragment(new InputHostnameFragment());
  }

  @Override
  protected int getLayoutContainerForFragment() {
    return R.id.content;
  }

  @Override
  protected void onBackPressedNotHandled() {
    moveTaskToBack(true);
  }
}
