package chat.rocket.android.authentication.matchers

import android.view.View
import android.widget.EditText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


fun withHint(expectedHint: String): Matcher<View> {
    return object : TypeSafeMatcher<View>() {

        override fun matchesSafely(view: View): Boolean {
            if (view !is EditText) {
                return false
            }

            val hint = view.hint.toString()
            return expectedHint == hint
        }

        override fun describeTo(description: Description) {}
    }
}