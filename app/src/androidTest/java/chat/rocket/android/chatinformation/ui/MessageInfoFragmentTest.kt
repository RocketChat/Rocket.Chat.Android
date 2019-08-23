package chat.rocket.android.chatinformation.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.RecyclerViewItemCountAssertion.Companion.withItemCount
import chat.rocket.android.util.ScrollToTop
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.loginUserToTheApp
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config
import testConfig.Config.Companion.TEST_USER2


class MessageInfoFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
                chat.rocket.android.authentication.login.ui.newInstance(Config.SERVER_URL)
            }
            loginUserToTheApp()
            navigateToTestUser()
        } catch (e: NoMatchingViewException) {
            Thread.sleep(3000)
            navigateToTestUser()
        }
    }

    @Test
    fun check_UI_elements() {
        onView(withId(R.id.text_toolbar_title)).check(matches(isDisplayed()))
        onView(withId(R.id.root_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun receipt_list_is_displayed() {
        onView(withId(R.id.recycler_view)).perform(ScrollToTop())
        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )
        onView(withText(R.string.action_info)).perform(click())
        Thread.sleep(3000)
        onView(withId(R.id.receipt_list)).check(withItemCount(equalTo(0)))
    }

    @Test
    fun check_toolbar_title() {
        onView(withId(R.id.recycler_view)).perform(ScrollToTop())
        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, click()
            )
        )
        onView(withText(R.string.action_info)).check(matches(isDisplayed()))
            .perform(click())
        onView(withId(R.id.text_toolbar_title)).check(matches(withText(R.string.message_information_title)))
    }

    private fun navigateToTestUser() {
        onView(withText(TEST_USER2)).perform(click())
        Thread.sleep(2000)
    }
}