package chat.rocket.android.widget;


import android.content.Intent;
import android.os.Bundle;
import com.klinker.android.simple_videoview.SimpleVideoView;
import com.klinker.android.sliding.SlidingActivity;

/**
 * Activity just for playing video.
 */
public class SimpleVideoViewActivity extends SlidingActivity {
  public static final String KEY_URL = "url";
  public static final String KEY_COOKIE = "cookie";

  private SimpleVideoView videoView;

  @Override
  public void init(Bundle savedInstanceState) {
    disableHeader();
    enableFullscreen();

    setContent(R.layout.activity_simple_video_view);
    videoView = (SimpleVideoView) findViewById(R.id.video);
    startVideo();
  }

  private void startVideo() {
    Intent intent = getIntent();
    String url = intent.getStringExtra(KEY_URL);
    String cookie = intent.getStringExtra(KEY_COOKIE);

    videoView.start(url, cookie);
  }

  @Override
  protected void onStop() {
    if (videoView != null) {
      videoView.release();
    }
    super.onStop();
  }
}
