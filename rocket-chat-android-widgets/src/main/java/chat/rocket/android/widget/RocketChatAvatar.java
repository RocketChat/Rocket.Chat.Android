package chat.rocket.android.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import chat.rocket.android.widget.R;

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
    LayoutInflater.from(context)
        .inflate(R.layout.message_avatar, this, true);

    draweeView = (SimpleDraweeView) findViewById(R.id.drawee_avatar);
  }

  public void loadImage(Drawable drawable) {
    final GenericDraweeHierarchy hierarchy = draweeView.getHierarchy();
    hierarchy.setImage(drawable, 100, true); // Is there a better way?
  }

  public void loadImage(String url, Drawable placeholder) {
    final GenericDraweeHierarchy hierarchy = draweeView.getHierarchy();
    hierarchy.setPlaceholderImage(placeholder);
    hierarchy.setFailureImage(placeholder);

    final DraweeController controller = Fresco.newDraweeControllerBuilder()
        .setUri(Uri.parse(url))
        .setAutoPlayAnimations(true)
        .build();
    draweeView.setController(controller);
  }
}
