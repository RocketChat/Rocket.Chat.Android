package chat.rocket.android.widget.helper

import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.graphics.drawable.VectorDrawableCompat
import chat.rocket.android.widget.R
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.view.SimpleDraweeView

object FrescoHelper {

    fun loadImage(simpleDraweeView: SimpleDraweeView, imageUri: String, placeholderDrawable: Drawable) {
        simpleDraweeView.hierarchy.setPlaceholderImage(placeholderDrawable)
        simpleDraweeView.controller = Fresco.newDraweeControllerBuilder().setUri(imageUri).setAutoPlayAnimations(true).build()
    }

    /** TODO
     * Replace with:
     *  fun loadImageWithCustomization(draweeView: SimpleDraweeView,
     *                                        imageUri: String,
     *                                        placeholderImageDrawableId : Int = R.drawable.image_dummy,
     *                                        failureImageDrawableId: Int = R.drawable.image_error) {
     *        [...]
     *  }
     *  It is need to convert java files which uses loadImageWithCustomization(...) method to use the above method signature.
     *  See: https://kotlinlang.org/docs/reference/functions.html#default-arguments.
     */
    fun loadImageWithCustomization(draweeView: SimpleDraweeView, imageUri: String) {
        val hierarchy: GenericDraweeHierarchy = draweeView.hierarchy
        hierarchy.setPlaceholderImage(VectorDrawableCompat.create(draweeView.resources, R.drawable.image_dummy, null))
        hierarchy.setFailureImage(VectorDrawableCompat.create(draweeView.resources, R.drawable.image_error, null))
        hierarchy.setProgressBarImage(ProgressBarDrawable())

        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(imageUri))
                .setAutoPlayAnimations(true)
                .setTapToRetryEnabled(true)
                .build()

        draweeView.controller = controller
    }
}