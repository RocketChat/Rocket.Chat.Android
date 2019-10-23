package chat.rocket.android.settings.password.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.settings.password.ui.PasswordFragment.Companion.newInstance
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.PASSWORD


class PasswordFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(PasswordActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        rule().activity.addFragmentBackStack(TAG_PASSWORD_FRAGMENT, R.id.fragment_container) {
            newInstance()
        }
        Thread.sleep(2000)
    }

    @Test
    fun check_UI_elements() {
        onView(withId(R.id.text_new_password)).check(matches(isDisplayed()))
        onView(withId(R.id.text_confirm_password)).check(matches(isDisplayed()))
    }

    @Test
    fun change_password() {
        onView(withId(R.id.text_new_password)).perform(
            typeText(PASSWORD), closeSoftKeyboard()
        )
        onView(withId(R.id.text_confirm_password)).perform(
            typeText(PASSWORD), closeSoftKeyboard()
        )
        onView(withId(R.id.action_password)).perform(click())
    }
}