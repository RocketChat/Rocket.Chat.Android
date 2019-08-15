package chat.rocket.android.chatrooms.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.loginUserToTheApp
import chat.rocket.android.util.withIndex
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.TEST_CHANNEL

class ChatRoomsFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
                chat.rocket.android.authentication.login.ui.newInstance(SERVER_URL)
            }
            loginUserToTheApp()
        } catch (e: NoMatchingViewException) {
            Thread.sleep(3000)
        }
    }

    @Test
    fun check_UI_elements() {
        onView(withId(R.id.recycler_view)).check(matches(isDisplayed()))
        onView(withIndex(withId(R.id.image_avatar), 0)).check(matches(isDisplayed()))
        onView(withIndex(withId(R.id.image_chat_icon), 0)).check(matches(isDisplayed()))
        onView(withIndex(withId(R.id.text_last_message), 0)).check(matches(isDisplayed()))
        onView(withIndex(withId(R.id.text_timestamp), 0)).check(matches(isDisplayed()))
        onView(withId(R.id.text_sort_by)).check(matches(isDisplayed()))
    }

    @Test
    fun clicking_channel_should_open_chatroom() {
        onView(withText(TEST_CHANNEL)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).check(matches(withText(TEST_CHANNEL)))
        onView(withId(R.id.message_list_container)).check(matches(isDisplayed()))
    }
}