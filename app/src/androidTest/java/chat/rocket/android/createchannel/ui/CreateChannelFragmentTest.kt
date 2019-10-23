package chat.rocket.android.createchannel.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.loginUserToTheApp
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.SERVER_URL


class CreateChannelFragmentTest {
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
            onView(withId(R.id.action_new_channel)).perform(click())
        } catch (e: NoMatchingViewException) {
            Thread.sleep(3000)
            onView(withId(R.id.action_new_channel)).perform(click())
        }
    }

    @Test
    fun check_UI_element() {
        onView(withId(R.id.text_channel_type)).check(matches(isDisplayed()))
        onView(withId(R.id.text_channel_type_description)).check(matches(isDisplayed()))
        onView(withId(R.id.switch_channel_type)).check(matches(isDisplayed()))
        onView(withId(R.id.text_read_only)).check(matches(isDisplayed()))
        onView(withId(R.id.text_read_only_description)).check(matches(isDisplayed()))
        onView(withId(R.id.switch_read_only)).check(matches(isDisplayed()))
        onView(withId(R.id.image_channel_icon)).check(matches(isDisplayed()))
        onView(withId(R.id.text_channel_name)).check(matches(isDisplayed()))
        onView(withId(R.id.image_invite_member)).check(matches(isDisplayed()))
        onView(withId(R.id.text_invite_members)).check(matches(isDisplayed()))
    }

    @Test
    fun channel_should_be_public() {
        onView(withId(R.id.text_channel_type)).check(matches(withText("Public")))
        onView(withId(R.id.text_channel_type_description)).check(matches(withText(R.string.msg_public_channel_description)))
    }

    @Test
    fun channel_description_should_change_on_making_channel_private() {
        onView(withId(R.id.switch_channel_type)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.text_channel_type)).check(matches(withText("Private")))
        onView(withId(R.id.text_channel_type_description)).check(matches(withText(R.string.msg_private_channel_description)))
        Thread.sleep(1000)
        onView(withId(R.id.switch_channel_type)).perform(click())
    }
}