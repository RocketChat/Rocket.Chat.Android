package chat.rocket.android.chatrooms.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.matchers.withIndex
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.EXISTING_CHANNEL
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.USERNAME

class ChatRoomsFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            loginIfUserIsLoggedOut()
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
        onView(withText(EXISTING_CHANNEL)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).check(matches(withText(EXISTING_CHANNEL)))
        onView(withId(R.id.message_list_container)).check(matches(isDisplayed()))
    }

    private fun loginIfUserIsLoggedOut() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.login.ui.newInstance(SERVER_URL)
        }
        onView(withId(R.id.text_username_or_email)).perform(
            typeText(USERNAME),
            closeSoftKeyboard()
        )
        onView(withId(R.id.text_password)).perform(typeText(PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.button_log_in)).perform(click())
        Thread.sleep(12000)
    }
}