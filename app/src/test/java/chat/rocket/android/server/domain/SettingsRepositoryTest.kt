package chat.rocket.android.server.domain

import chat.rocket.core.model.Value
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SettingsRepositoryTest {
    @Test
    fun `uploadMimeFilter returns null if not specified`() {
        val settings = emptyMap<String, Value<Any>>()
        val filter = settings.uploadMimeTypeFilter()
        assertThat(filter).isNull()
    }
}