package chat.rocket.android.util.avatar

import android.graphics.drawable.Drawable
import chat.rocket.android.widget.AvatarTextDrawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import timber.log.Timber

class AvatarDrawableTranscoder : ResourceTranscoder<Avatar, Drawable> {
    override fun transcode(toTranscode: Resource<Avatar>, options: Options): Resource<Drawable>? {
        val avatar =  toTranscode.get()
        Timber.d("Transcoding avatar: $avatar")
        return SimpleResource<Drawable>(AvatarTextDrawable(avatar.initial, avatar.color))
    }
}