package chat.rocket.android.widget.helper

import android.net.Uri
import android.support.graphics.drawable.VectorDrawableCompat
import chat.rocket.android.widget.R
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.facebook.drawee.view.SimpleDraweeView

class FrescoAvatarHelper {

    companion object {

        @JvmStatic fun loadImage(draweeView: SimpleDraweeView, imageUrl: String) {
            val hierarchy = draweeView.hierarchy
            hierarchy.setPlaceholderImage(VectorDrawableCompat.create(draweeView.resources, R.drawable.ic_avatar_placeholder, null))
            hierarchy.setFailureImage(VectorDrawableCompat.create(draweeView.resources, R.drawable.ic_avatar_failure, null))
            hierarchy.setProgressBarImage(ProgressBarDrawable())

            val controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(imageUrl))
                    .setOldController(draweeView.controller)
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