package chat.rocket.android.sortingandgrouping.ui

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.matchers.RecyclerViewItemCountAssertion
import chat.rocket.android.matchers.clickChildViewWithId
import chat.rocket.android.matchers.withTextInChild
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.CHANNELS
import testConfig.Config.Companion.DIRECT_MESSAGES
import testConfig.Config.Companion.PASSWORD
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.USERNAME

class SortingAndGroupingBottomSheetFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(AuthenticationActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        try {
            loginIfUserIsLoggedOut()
            onView(withId(R.id.text_sort_by)).perform(click())
        } catch (e: NoMatchingViewException) {
            onView(withId(R.id.text_sort_by)).perform(click())
        }
    }

    @Test
    fun check_UI_elements() {
        onView(withId(R.id.text_sort_by)).check(matches(isDisplayed()))
        onView(withId(R.id.text_name)).check(matches(isDisplayed()))
        onView(withId(R.id.text_activity)).check(matches(isDisplayed()))
        onView(withId(R.id.view_divider)).check(matches(isDisplayed()))
        onView(withId(R.id.text_unread_on_top)).check(matches(isDisplayed()))
        onView(withId(R.id.text_group_by_type)).check(matches(isDisplayed()))
        onView(withId(R.id.text_group_by_favorites)).check(matches(isDisplayed()))
    }

    @Test
    fun sort_by_name() {
        onView(withId(R.id.text_name)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.recycler_view)).check(
            RecyclerViewItemCountAssertion.withItemCount(greaterThan(0))
        )
        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, withTextInChild(R.id.text_chat_name, "c239dk")
            )
        )
    }

    @Test
    fun sort_by_activity() {
        onView(withId(R.id.text_activity)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.recycler_view)).check(
            RecyclerViewItemCountAssertion.withItemCount(greaterThan(0))
        )
        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, clickChildViewWithId(R.id.text_chat_name)
            )
        )
    }

    @Test
    fun group_by_type() {
        onView(withId(R.id.text_group_by_type)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.recycler_view)).check(
            RecyclerViewItemCountAssertion.withItemCount(greaterThan(0))
        )
        onView(withText(CHANNELS)).check(matches(isDisplayed()))
        onView(withText(DIRECT_MESSAGES)).check(matches(isDisplayed()))
        onView(withId(R.id.text_sort_by)).perform(click())
        onView(withId(R.id.text_group_by_type)).perform(click())
        Espresso.pressBack()
    }

    private fun loginIfUserIsLoggedOut() {
        rule().activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.login.ui.newInstance(SERVER_URL)
        }
        onView(withId(R.id.text_username_or_email)).perform(
            typeText(USERNAME), closeSoftKeyboard()
        )
        onView(withId(R.id.text_password)).perform(typeText(PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.button_log_in)).perform(click())
        Thread.sleep(12000)
    }
}