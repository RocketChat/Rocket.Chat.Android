package chat.rocket.android.authentication.loginoptions.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.authentication.ui.AuthenticationActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginOptionsFragmentTest {

    @Rule
    @JvmField
    var activityRule = ActivityTestRule<AuthenticationActivity>(AuthenticationActivity::class.java)

    @Before
    fun setUp() {
        onView(withId(R.id.join_community_container))
            .perform(scrollTo(), click())
        Thread.sleep(5000)
    }

    @Test
    fun check_UI_element() {
        onView(withId(R.id.button_facebook)).check(matches(withText("Continue with Facebook")))
        onView(withId(R.id.button_google)).check(matches(withText("Continue with Google")))
        onView(withId(R.id.button_gitlab)).check(matches(withText("Continue with GitLab")))
    }

    @Test
    fun expand_collapse_accounts_and_check_UI_elements() {
        onView(withId(R.id.button_expand_collapse_accounts))
            .perform(scrollTo(), click())
        onView(withId(R.id.button_linkedin)).check(matches(withText("Continue with LinkedIn")))
        onView(withId(R.id.button_github)).check(matches(withText("Continue with GitHub")))
    }

    @Test
    fun click_login_with_email() {
        onView(withId(R.id.button_login_with_email)).check(matches(withText("Login with e-mail")))
            .perform(scrollTo(), click())
        onView(withId(R.id.text_login)).check(matches(withText("Login")))
    }

    @Test
    fun check_create_an_account() {
        onView(withId(R.id.button_create_an_account)).check(matches(withText("Create an account")))
            .perform(scrollTo(), click())
        onView(withId(R.id.text_sign_up)).check(matches(withText("Sign up")))
    }

    @Test
    fun check_facebook_button() {
        onView(withId(R.id.button_facebook))
            .perform(scrollTo(), click())
        onView(withId(R.id.web_view)).check(matches(isDisplayed()))
    }

    @Test
    fun check_github_button() {
        onView(withId(R.id.button_github))
            .perform(scrollTo(), click())
        onView(withId(R.id.web_view)).check(matches(isDisplayed()))
    }

    @Test
    fun check_google_button() {
        onView(withId(R.id.button_google)).perform(scrollTo())
            .perform(scrollTo(), click())
        onView(withId(R.id.web_view)).check(matches(isDisplayed()))
    }

    @Test
    fun check_linkedin_button() {
        onView(withId(R.id.button_expand_collapse_accounts))
            .perform(scrollTo(), click())
        onView(withId(R.id.button_linkedin))
            .perform(scrollTo(), click())
        onView(withId(R.id.web_view)).check(matches(isDisplayed()))
    }

    @Test
    fun check_gitlab_button() {
        onView(withId(R.id.button_expand_collapse_accounts))
            .perform(scrollTo(), click())
        onView(withId(R.id.button_gitlab))
            .perform(scrollTo(), click())
        onView(withId(R.id.web_view)).check(matches(isDisplayed()))
    }
}