package chat.rocket.android.helper

import androidx.databinding.BindingAdapter
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView

@BindingAdapter("roundedCornerRadius")
fun setRoundedCornerRadius(view: SimpleDraweeView, height: Float) {
    val roundingParams = RoundingParams.fromCornersRadius(height)
    view.hierarchy.roundingParams = roundingParams
}