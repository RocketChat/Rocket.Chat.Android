package chat.rocket.android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import chat.rocket.android.widget.helper.FrescoHelper;
import com.facebook.drawee.view.SimpleDraweeView;

public class RocketChatAvatar extends FrameLayout {

  private SimpleDraweeView draweeView;

  public RocketChatAvatar(Context context) {
    super(context);
    initialize(context, null);
  }

  public RocketChatAvatar(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs);
  }

  public RocketChatAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RocketChatAvatar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    LayoutInflater.from(context).inflate(R.layout.message_avatar, this, true);
    draweeView = findViewById(R.id.drawee_avatar);
  }

  public void loadImage(String imageUri) {
    FrescoHelper.loadImage(draweeView, imageUri);
  }
}