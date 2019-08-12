//package chat.rocket.android.settings.ui
//
//import android.content.Intent
//import android.net.Uri
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.NoMatchingViewException
//import androidx.test.espresso.action.ViewActions.*
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.intent.Intents
//import androidx.test.espresso.intent.Intents.intended
//import androidx.test.espresso.intent.matcher.IntentMatchers.*
//import androidx.test.espresso.matcher.ViewMatchers.*
//import androidx.test.platform.app.InstrumentationRegistry
//import androidx.test.rule.ActivityTestRule
//import androidx.test.uiautomator.UiDevice
//import chat.rocket.android.R
//import chat.rocket.android.analytics.event.ScreenViewEvent
//import chat.rocket.android.authentication.ui.AuthenticationActivity
//import chat.rocket.android.util.extensions.addFragmentBackStack
//import org.hamcrest.CoreMatchers.allOf
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import testConfig.Config.Companion.PASSWORD
//import testConfig.Config.Companion.SERVER_URL
//import testConfig.Config.Companion.USERNAME
//
//class SettingsFragmentIntentTest {
//
//    @JvmField
//    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)
//
//    @Rule
//    fun rule() = activityRule
//
//    @Before
//    fun setUp() {
//        try {
//            loginIfUserIsLoggedOut()
//            navigateToSettings()
//        } catch (e: NoMatchingViewException) {
//            navigateToSettings()
//        }
//    }
//
//    @Test
//    fun check_review_the_app() {
//        Intents.init()
//        val mDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
//        onView(withId(R.id.text_review_this_app)).perform(click())
//        intended(allOf(
//            hasAction(Intent.ACTION_VIEW),
//            hasData(Uri.parse("market://details?id=chat.rocket.android"))
//        ))
//        Thread.sleep(5000)
//        mDevice.pressBack()
//        mDevice.pressBack()
//        Intents.release()
//    }
//
//    @Test
//    fun check_contact_us_button() {
//        Intents.init()
//        val mDevice: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
//        onView(withId(R.id.text_contact_us)).perform(click())
//        intended(allOf(
//            hasAction(Intent.ACTION_CHOOSER),
//            hasExtra(Intent.EXTRA_TITLE, "Send email")
//        ))
//        Thread.sleep(5000)
//        mDevice.pressBack()
//        mDevice.pressBack()
//        Intents.release()
//    }
//
//    private fun loginIfUserIsLoggedOut() {
//        rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
//            chat.rocket.android.authentication.login.ui.newInstance(SERVER_URL)
//        }
//        onView(withId(R.id.text_username_or_email)).perform(
//            typeText(USERNAME), closeSoftKeyboard()
//        )
//        onView(withId(R.id.text_password)).perform(typeText(PASSWORD), closeSoftKeyboard())
//        onView(withId(R.id.button_log_in)).perform(click())
//        Thread.sleep(12000)
//    }
//
//    private fun navigateToSettings() {
//        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
//        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())
//        Thread.sleep(3000)
//    }
//}
