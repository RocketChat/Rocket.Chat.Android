package chat.rocket.android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import chat.rocket.android.widget.helper.FrescoHelper;

public class RocketChatAvatar extends FrameLayout {

  private SimpleDraweeView simpleDraweeViewAvatar;

  public RocketChatAvatar(Context context) {
    super(context);
    initialize(context);
  }

  public RocketChatAvatar(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  public RocketChatAvatar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public RocketChatAvatar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context);
  }

  private void initialize(Context context) {
    LayoutInflater.from(context).inflate(R.layout.message_avatar, this, true);
    simpleDraweeViewAvatar = findViewById(R.id.drawee_avatar);
  }

  public void loadImage(String imageUri, Drawable placeholderDrawable) {
    FrescoHelper.INSTANCE.loadImage(simpleDraweeViewAvatar, imageUri, placeholderDrawable);
  }
}