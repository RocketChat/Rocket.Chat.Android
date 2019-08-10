package chat.rocket.android.userdetails.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.util.clickChildViewWithId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.EXISTING_USER2

class UserDetailsFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(MainActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        Thread.sleep(5000)
        onView(withText(EXISTING_USER2)).perform(click())
        Thread.sleep(5000)
        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                clickChildViewWithId(R.id.image_avatar)
            )
        )
        Thread.sleep(4000)
    }

    @Test
    fun check_UI_elements() {
        Thread.sleep(8000)
        onView(withId(R.id.image_avatar)).check(matches(isDisplayed()))
        onView(withId(R.id.image_blur)).check(matches(isDisplayed()))
        onView(withId(R.id.image_arrow_back)).check(matches(isDisplayed()))
        onView(withId(R.id.text_message)).check(matches(withText("Message")))
        onView(withId(R.id.text_video_call)).check(matches(withText("Video call")))
        onView(withId(R.id.text_title_status)).check(matches(withText("Status")))
        onView(withId(R.id.text_title_timezone)).check(matches(withText("Timezone")))
        onView(withId(R.id.text_description_timezone)).check(matches(isDisplayed()))
        onView(withId(R.id.button_remove_user)).check(matches(isDisplayed()))
    }

    @Test
    fun click_back_and_move_to_chatList() {
        Espresso.pressBack()
        onView(withId(R.id.message_list_container)).check(matches(isDisplayed()))
    }
}