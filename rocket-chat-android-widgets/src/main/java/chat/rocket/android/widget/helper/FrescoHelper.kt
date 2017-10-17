package chat.rocket.android.widget.helper

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.support.graphics.drawable.VectorDrawableCompat
import chat.rocket.android.widget.R
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.generic.GenericDraweeHierarchy
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView

object FrescoHelper {

    fun loadImage(simpleDraweeView: SimpleDraweeView, imageUri: String, placeholderDrawable: Drawable) {
        // ref: https://github.com/facebook/fresco/issues/501
        if (placeholderDrawable is ShapeDrawable) {
            placeholderDrawable.setPadding(Rect())
        }
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
        hierarchy.roundingParams = RoundingParams().setCornersRadii(5F, 5F, 5F, 5F)
        hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_CENTER
        hierarchy.setProgressBarImage(ProgressBarDrawable())

        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(imageUri))
                .setAutoPlayAnimations(true)
                .setTapToRetryEnabled(true)
                .build()

        draweeView.controller = controller
    }
}