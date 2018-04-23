package chat.rocket.android.util

import okhttp3.HttpUrl

fun convertSchemeToLower(url: String): String {

        val httpUrl = HttpUrl.parse(url)
        val scheme = httpUrl?.scheme()?.toLowerCase()
       
        return httpUrl?.newBuilder()
            ?.scheme(scheme)
            ?.build().toString()
    }
