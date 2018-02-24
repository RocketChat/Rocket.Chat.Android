package chat.rocket.android.core

import android.content.Context
import android.graphics.drawable.Drawable
import chat.rocket.android.util.avatar.Avatar
import chat.rocket.android.util.avatar.AvatarDecoder
import chat.rocket.android.util.avatar.AvatarDrawableTranscoder
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import java.nio.ByteBuffer

@GlideModule
class RocketChatGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.register(Avatar::class.java, Drawable::class.java, AvatarDrawableTranscoder())
                .prepend(ByteBuffer::class.java, Avatar::class.java, AvatarDecoder())
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
    }
}