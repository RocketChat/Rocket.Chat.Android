package chat.rocket.android.server.domain

import chat.rocket.core.model.Value
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class `SettingsRepository UploadMimeTypeFilter WhitelistIsSet Test`(private val allowedMimeTypes: String,
                                                                    private val expectedFilter: Array<String>?) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "\"{0}\"")
        fun data(): Collection<Array<Any?>> = listOf(
                arrayOf<Any?>("", null),
                arrayOf<Any?>(" ", null),
                arrayOf<Any?>("image/*", arrayOf("image/*")),
                arrayOf<Any?>("image/*,video/*", arrayOf("image/*", "video/*")),
                arrayOf<Any?>("image/*, video/*", arrayOf("image/*", "video/*")),
                arrayOf<Any?>("image/*,\tvideo/*", arrayOf("image/*", "video/*"))
        )
    }

    @Test
    fun test() {
        val settings = mapOf<String, Value<Any>>(Pair(UPLOAD_WHITELIST_MIMETYPES, Value(allowedMimeTypes)))
        val filter = settings.uploadMimeTypeFilter()
        assertThat(filter).isEqualTo(expectedFilter)
    }
}

