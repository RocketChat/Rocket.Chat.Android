package chat.rocket.android.profile.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.loginUserToTheApp
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.AWAY
import testConfig.Config.Companion.BUSY
import testConfig.Config.Companion.CHANGE_STATUS
import testConfig.Config.Companion.EMAIL
import testConfig.Config.Companion.INVISIBLE
import testConfig.Config.Companion.NAME
import testConfig.Config.Companion.ONLINE
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.USERNAME

@LargeTest
class ProfileFragmentTest {

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
            navigateToProfileFragment()
        } catch (e: NoMatchingViewException) {
            Thread.sleep(4000)
            navigateToProfileFragment()
        }
    }

    @Test
    fun check_UI_element() {
        onView(withId(R.id.layout_avatar_profile)).check(matches(isDisplayed()))
        onView(withId(R.id.text_status)).check(matches(isDisplayed()))
        onView(withId(R.id.text_name)).check(matches(isDisplayed()))
        onView(withId(R.id.text_email)).check(matches(isDisplayed()))
        onView(withId(R.id.text_username)).check(matches(isDisplayed()))
    }

    @Test
    fun check_name_of_logged_in_user() {
        onView(withId(R.id.text_name)).check(matches(withText(NAME)))
        onView(withId(R.id.text_name)).check(matches(withHint("Please enter your name…")))
    }

    @Test
    fun check_username_of_logged_in_user() {
        onView(withId(R.id.text_username)).check(matches(withText(USERNAME)))
        onView(withId(R.id.text_username)).check(matches(withHint("Please enter your username")))
    }

    @Test
    fun check_email_of_logged_in_user() {
        onView(withId(R.id.text_email)).check(matches(withText(EMAIL)))
        onView(withId(R.id.text_email)).check(matches(withHint("Please enter your email address")))
    }

    @Test
    fun user_status_should_be_online() {
        onView(withId(R.id.text_status)).perform(click())
        onView(withText(ONLINE)).perform(click())
        onView(withText(CHANGE_STATUS)).perform(click())
        onView(withId(R.id.text_status)).check(matches(withText("Status: Online")))
    }

    @Test
    fun user_status_should_be_away() {
        onView(withId(R.id.text_status)).perform(click())
        onView(withText(AWAY)).perform(click())
        onView(withText(CHANGE_STATUS)).perform(click())
        onView(withId(R.id.text_status)).check(matches(withText("Status: Away")))
    }

    @Test
    fun user_status_should_be_busy() {
        onView(withId(R.id.text_status)).perform(click())
        onView(withText(BUSY)).perform(click())
        onView(withText(CHANGE_STATUS)).perform(click())
        onView(withId(R.id.text_status)).check(matches(withText("Status: Busy")))
    }

    @Test
    fun user_status_should_be_Invisible() {
        onView(withId(R.id.text_status)).perform(click())
        onView(withText(INVISIBLE)).perform(click())
        onView(withText(CHANGE_STATUS)).perform(click())
        onView(withId(R.id.text_status)).check(matches(withText("Status: Offline")))
    }

    private fun navigateToProfileFragment() {
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.image_avatar)).perform(click())
        Thread.sleep(2000)
    }
}