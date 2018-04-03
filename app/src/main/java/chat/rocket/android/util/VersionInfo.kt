package chat.rocket.android.util

data class VersionInfo(
        val major: Int,
        val minor: Int,
        val update: Int = 0,
        val release: String?,
        val full: String
)