package chat.rocket.android.webview.adminpanel.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.util.extensions.addFragmentBackStack
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testConfig.Config.Companion.ADMIN_PANEL_URL
import testConfig.Config.Companion.USER_TOKEN

class AdminPanelWebViewFragmentTest {

    @JvmField
    var activityRule = ActivityTestRule(MainActivity::class.java, true, true)

    @Rule
    fun rule() = activityRule

    @Before
    fun setUp() {
        Thread.sleep(3000)
        rule().activity.addFragmentBackStack(TAG_ADMIN_PANEL_WEB_VIEW_FRAGMENT, R.id.fragment_container) {
            newInstance(ADMIN_PANEL_URL, USER_TOKEN)
        }
    }

    @Test
    fun check_webview_is_opened() {
        onView(withId(R.id.web_view)).check(matches(isDisplayed()))
    }
}