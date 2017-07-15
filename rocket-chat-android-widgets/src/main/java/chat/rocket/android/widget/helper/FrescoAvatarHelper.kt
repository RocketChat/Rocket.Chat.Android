package chat.rocket.android.widget.helper

import android.net.Uri
import android.support.graphics.drawable.VectorDrawableCompat
import chat.rocket.android.widget.R
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.facebook.drawee.view.SimpleDraweeView

class FrescoAvatarHelper {

    companion object {

        @JvmStatic fun setupDraweeAndLoadImage(draweeView: SimpleDraweeView, imageUrl: String) {
            FrescoAvatarHelper.setupDrawee(draweeView)
            FrescoAvatarHelper.loadImage(imageUrl, draweeView)
        }

        @JvmStatic fun setupDrawee(draweeView: SimpleDraweeView) {
            val hierarchy = draweeView.hierarchy
            hierarchy.setPlaceholderImage(VectorDrawableCompat.create(draweeView.resources, R.drawable.ic_avatar_placeholder, null))
            hierarchy.setFailureImage(VectorDrawableCompat.create(draweeView.resources, R.drawable.ic_avatar_failure, null))
            hierarchy.setProgressBarImage(ProgressBarDrawable())
        }

        @JvmStatic fun loadImage(imageUrl: String, draweeView: SimpleDraweeView) {
            val controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(imageUrl))
                    .setAutoPlayAnimations(true)
                    .setTapToRetryEnabled(true)
                    .build()
            draweeView.controller = controller
        }

        @JvmStatic fun showFailureImage(draweeView: SimpleDraweeView) {
            val hierarchy = draweeView.hierarchy
            hierarchy.setPlaceholderImage(VectorDrawableCompat.create(draweeView.resources, R.drawable.ic_avatar_failure, null))
            hierarchy.setFailureImage(VectorDrawableCompat.create(draweeView.resources, R.drawable.ic_avatar_failure, null))

            val controller = Fresco.newDraweeControllerBuilder()
                    .setAutoPlayAnimations(true)
                    .build()
            draweeView.controller = controller
        }
    }
}