package chat.rocket.android.settings.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.USERNAME
import testConfig.Config.Companion.serverUrl


class SettingsFragmentIntentTest {

    @JvmField
    var activityRule = IntentsTestRule(AuthenticationActivity::class.java)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            loginIfUserIsLoggedOut()
            navigateToSettings()
            Intents.intending(CoreMatchers.not(isInternal()))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        } catch (e: NoMatchingViewException) {
            navigateToSettings()
            Intents.intending(CoreMatchers.not(isInternal()))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        }
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun check_review_the_app() {
        onView(withId(R.id.text_review_this_app)).perform(click())
        intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse("market://details?id=chat.rocket.android"))
        ))
    }

    @Test
    fun check_contact_us_button() {
        onView(withId(R.id.text_contact_us)).perform(click())
        intended(allOf(
            hasAction(Intent.ACTION_CHOOSER),
            hasExtra(Intent.EXTRA_TITLE, "Send email")
        ))
        pressBack()
        pressBack()
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