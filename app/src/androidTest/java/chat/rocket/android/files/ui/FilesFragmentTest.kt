package chat.rocket.android.files.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.matchers.RecyclerViewItemCountAssertion.Companion.withItemCount
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.FILES
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.USERNAME

class FilesFragmentTest {

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
    fun check_toolbar_is_displayed() {
        navigateToGeneralChannelDetails()
        onView(withText(FILES)).perform(click())
        Thread.sleep(5000)
        onView(withId(R.id.text_toolbar_title)).check(matches(isDisplayed()))
    }

    @Test
    fun no_of_files_should_be_zero() {
        navigateToDummyUserChannelDetails()
        onView(withText(FILES)).perform(click())
        Thread.sleep(6000)
        onView(withId(R.id.recycler_view)).check(withItemCount(equalTo(0)))
        onView(withId(R.id.image_file)).check(matches(isDisplayed()))
        onView(withId(R.id.text_no_file)).check(matches(isDisplayed()))
        onView(withId(R.id.text_all_files_appear_here)).check(matches(isDisplayed()))
    }

    @Test
    fun no_of_files_should_be_greater_than_zero() {
        navigateToGeneralChannelDetails()
        onView(withText(FILES)).perform(click())
        Thread.sleep(6000)
        onView(withId(R.id.recycler_view)).check(withItemCount(greaterThan(0)))
        onView(withId(R.id.image_file)).check(matches(not(isDisplayed())))
        onView(withId(R.id.text_no_file)).check(matches(not(isDisplayed())))
        onView(withId(R.id.text_all_files_appear_here)).check(matches(not(isDisplayed())))
    }

    private fun loginIfUserIsLoggedOut() {
        rule().activity.addFragmentBackStack(
            ScreenViewEvent.Login.screenName,
            R.id.fragment_container
        ) {
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

    private fun navigateToGeneralChannelDetails() {
        Thread.sleep(5000)
        onView(withText("general")).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).perform(click())
    }

    private fun navigateToDummyUserChannelDetails() {
        Thread.sleep(5000)
        onView(withText("dfcxc")).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.text_toolbar_title)).perform(click())
    }
}