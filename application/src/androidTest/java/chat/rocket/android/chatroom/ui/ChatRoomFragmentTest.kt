package chat.rocket.android.chatroom.ui

import android.content.Intent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import android.app.Activity
import android.app.Instrumentation.ActivityResult
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Before

@LargeTest
class ChatRoomFragmentTest {

    @JvmField
    @Rule
    val activityRule = IntentsTestRule<ChatRoomActivity>(ChatRoomActivity::class.java, false, false)

    @Before
    fun stubAllExternalIntents() {
        val activityIntent = InstrumentationRegistry.getTargetContext()
            .chatRoomIntent("id", "name", "type", false, 0L)
        activityRule.launchActivity(activityIntent)
        intending(not(isInternal())).respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @Test
    fun showFileSelection_nonNullFiltersAreApplied() {
        val fragment =
            activityRule.activity.supportFragmentManager.findFragmentByTag("ChatRoomFragment") as ChatRoomFragment

        val filters = arrayOf("image/*")
        fragment.showFileSelection(filters)

        intended(
            allOf(
                hasAction(Intent.ACTION_GET_CONTENT),
                hasType("*/*"),
                hasCategories(setOf(Intent.CATEGORY_OPENABLE)),
                hasExtra(Intent.EXTRA_MIME_TYPES, filters)
            )
        )
    }

    @Test
    fun showFileSelection_nullFiltersAreNotApplied() {
        val fragment =
            activityRule.activity.supportFragmentManager.findFragmentByTag("ChatRoomFragment") as ChatRoomFragment

        fragment.showFileSelection(null)

        intended(
            allOf(
                hasAction(Intent.ACTION_GET_CONTENT),
                hasType("*/*"),
                hasCategories(setOf(Intent.CATEGORY_OPENABLE)),
                not(hasExtraWithKey(Intent.EXTRA_MIME_TYPES))
            )
        )
    }
}