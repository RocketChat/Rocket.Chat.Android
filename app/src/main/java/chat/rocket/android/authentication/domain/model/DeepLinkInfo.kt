package chat.rocket.android.authentication.domain.model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// see https://rocket.chat/docs/developer-guides/deeplink/ for documentation

const val DEEP_LINK_INFO_KEY = "deep_link_info"

@SuppressLint("ParcelCreator")
@Parcelize
data class DeepLinkInfo(
    val url: String,
    val userId: String?,
    val token: String?,
    val rid: String?,
    val roomType: String?,
    val roomName: String?
) : Parcelable
