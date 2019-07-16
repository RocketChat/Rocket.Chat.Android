package chat.rocket.android.settings.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.APP_VERSION
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.USERNAME
import testConfig.Config.Companion.serverUrl

class SettingsFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            loginIfUserIsLoggedOut()
            navigateToSettings()
        } catch (e: NoMatchingViewException) {
            navigateToSettings()
        }
    }

    @Test
    fun check_UI_elements() {
        onView(withId(R.id.text_display_name)).check(matches(isDisplayed()))
        onView(withId(R.id.text_status)).check(matches(isDisplayed()))
        onView(withId(R.id.image_avatar)).check(matches(isDisplayed()))
        onView(withId(R.id.text_contact_us)).check(matches(isDisplayed()))
        onView(withId(R.id.text_language)).check(matches(isDisplayed()))
        onView(withId(R.id.text_review_this_app)).check(matches(isDisplayed()))
        onView(withId(R.id.text_share_this_app)).check(matches(isDisplayed()))
        onView(withId(R.id.text_license)).check(matches(isDisplayed()))
        onView(withId(R.id.text_app_version)).check(matches(isDisplayed()))
        onView(withId(R.id.text_server_version)).check(matches(isDisplayed()))
        onView(withId(R.id.text_send_crash_report)).check(matches(isDisplayed()))
        onView(withId(R.id.text_send_crash_report_description)).check(matches(isDisplayed()))
        onView(withId(R.id.text_logout)).check(matches(isDisplayed()))
        onView(withId(R.id.text_delete_account)).check(matches(isDisplayed()))
    }

    @Test
    fun check_share_the_app() {
        onView(withId(R.id.text_share_this_app)).perform(click())
        val mDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        Thread.sleep(5000)
        val titleLabel: UiObject = mDevice.findObject(UiSelector().text("Share using"))
        if (!titleLabel.exists()) {
            throw RuntimeException("wrong title!")
        }
        mDevice.pressBack()
    }

    @Test
    fun check_license() {
        onView(withId(R.id.text_license)).perform(click())
        onView(withId(R.id.web_view)).check(matches(isDisplayed()))
    }

    @Test
    fun check_version_of_app() {
        onView(withId(R.id.text_app_version)).check(matches(withText(APP_VERSION)))
    }

    @Test
    fun change_language_to_german_then_reset_to_english() {
        onView(withId(R.id.text_language)).perform(click())
        onView(withText("German")).perform(click())
        Thread.sleep(2000)
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())
        Thread.sleep(3000)
        onView(withText("Sprache")).check(matches(isDisplayed())).perform(click())
        onView(withText("Deutsch")).perform(swipeDown()).perform(swipeDown()).perform(swipeDown()).perform(swipeDown())
        onView(withText("Englisch")).check(matches(isDisplayed())).perform(click())
    }

    private fun loginIfUserIsLoggedOut() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.login.ui.newInstance(serverUrl)
        }
        onView(withId(R.id.text_username_or_email)).perform(
            typeText(USERNAME), closeSoftKeyboard()
        )
        onView(withId(R.id.text_password)).perform(typeText(PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.button_log_in)).perform(click())
        Thread.sleep(12000)
    }

    private fun navigateToSettings() {
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())
        Thread.sleep(3000)
    }
}