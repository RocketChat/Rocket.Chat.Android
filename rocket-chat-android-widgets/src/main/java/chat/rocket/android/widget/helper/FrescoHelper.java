package chat.rocket.android.widget.helper;

import android.net.Uri;
import android.support.graphics.drawable.VectorDrawableCompat;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import chat.rocket.android.widget.R;

public class FrescoHelper {

  private FrescoHelper() {
  }

  public static void setupDraweeAndLoadImage(String imageUrl, SimpleDraweeView draweeView) {
    setupDrawee(draweeView);
    loadImage(imageUrl, draweeView);
  }

  public static void setupDrawee(SimpleDraweeView draweeView) {
    final GenericDraweeHierarchy hierarchy = draweeView.getHierarchy();
    hierarchy.setPlaceholderImage(VectorDrawableCompat.create(draweeView.getResources(), R.drawable.image_dummy, null));
    hierarchy.setFailureImage(VectorDrawableCompat.create(draweeView.getResources(), R.drawable.image_error, null));
    hierarchy.setProgressBarImage(new ProgressBarDrawable());
  }

  public static void loadImage(String imageUrl, SimpleDraweeView draweeView) {
    final DraweeController controller = Fresco.newDraweeControllerBuilder()
        .setUri(Uri.parse(imageUrl))
        .setAutoPlayAnimations(true)
        .setTapToRetryEnabled(true)
        .build();
    draweeView.setController(controller);
  }
}