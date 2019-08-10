package chat.rocket.android.directory.ui

import android.widget.AutoCompleteTextView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.RecyclerViewItemCountAssertion.Companion.withItemCount
import chat.rocket.android.util.loginUserToTheApp
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.CHANNELS
import testConfig.Config.Companion.DIRECTORY
import testConfig.Config.Companion.EXISTING_CHANNEL
import testConfig.Config.Companion.EXISTING_USER
import testConfig.Config.Companion.NON_EXISTING_CHANNEL
import testConfig.Config.Companion.NON_EXISTING_USER
import testConfig.Config.Companion.SERVER_URL
import testConfig.Config.Companion.USERS

class DirectoryFragmentTest {

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
            navigateToDirectory()
        } catch (e: NoMatchingViewException) {
            Thread.sleep(3000)
            navigateToDirectory()
        }
    }

    @Test
    fun check_UI_elements() {
        onView(withId(R.id.text_sort_by)).check(matches(isDisplayed()))
        onView(withId(R.id.layout_app_bar)).check(matches(isDisplayed()))
        onView(withText(CHANNELS)).check(matches(isDisplayed()))
    }

    @Test
    fun channels_should_be_greater_than_zero() {
        Thread.sleep(5000)
        onView(withId(R.id.recycler_view)).check(withItemCount(greaterThan(0)))
    }

    @Test
    fun users_should_be_greater_than_zero() {
        onView(withId(R.id.text_sort_by)).perform(click())
        onView(withText(USERS)).perform(click())
        Espresso.pressBack()
        Thread.sleep(8000)
        onView(withId(R.id.recycler_view)).check(withItemCount(greaterThan(0)))
        onView(withId(R.id.text_sort_by)).perform(click())
        onView(withText(CHANNELS)).perform(click())
        Espresso.pressBack()
    }

    @Test
    fun search_an_existing_channel() {
        onView(withId(R.id.action_search)).perform(click())
        onView(isAssignableFrom(AutoCompleteTextView::class.java)).perform(
            clearText(),
            typeText(EXISTING_CHANNEL),
            closeSoftKeyboard()
        )
        Thread.sleep(8000)
        onView(withId(R.id.recycler_view)).check(withItemCount(greaterThan(0)))
    }

    @Test
    fun search_a_non_existing_channel() {
        onView(withId(R.id.action_search)).perform(click())
        onView(isAssignableFrom(AutoCompleteTextView::class.java)).perform(
            clearText(),
            typeText(NON_EXISTING_CHANNEL),
            closeSoftKeyboard()
        )
        Thread.sleep(8000)
        onView(withId(R.id.recycler_view)).check(withItemCount(equalTo(0)))
    }

    @Test
    fun search_an_existing_user() {
        onView(withId(R.id.text_sort_by)).perform(click())
        onView(withText(USERS)).perform(click())
        Espresso.pressBack()
        onView(withId(R.id.action_search)).perform(click())
        onView(isAssignableFrom(AutoCompleteTextView::class.java)).perform(
            clearText(),
            typeText(EXISTING_USER),
            closeSoftKeyboard()
        )
        Espresso.pressBack()
        Thread.sleep(5000)
        onView(withId(R.id.recycler_view)).check(withItemCount(greaterThan(0)))
    }

    @Test
    fun search_a_non_existing_user() {
        onView(withId(R.id.action_search)).perform(click())
        Thread.sleep(3000)
        onView(isAssignableFrom(AutoCompleteTextView::class.java)).perform(
            clearText(),
            typeText(NON_EXISTING_USER),
            closeSoftKeyboard()
        )
        Espresso.pressBack()
        Thread.sleep(3000)
        onView(withId(R.id.recycler_view)).check(withItemCount(equalTo(0)))
    }

    private fun navigateToDirectory() {
        onView(withId(R.id.action_search)).perform(click())
        onView(withText(DIRECTORY)).perform(click())
    }
}