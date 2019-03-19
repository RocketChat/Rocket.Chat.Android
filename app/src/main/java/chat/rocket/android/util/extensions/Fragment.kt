package chat.rocket.android.util.extensions

import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

inline fun Fragment.ui(crossinline block: (activity: FragmentActivity) -> Unit): Job? {
    // Checking first for activity and view saves us from some synchronyzed and thread local checks
    if (activity != null && view != null && context != null) {
        // If we already are running on the Main Thread (UI Thread), just go ahead and execute the block
        return if (Looper.getMainLooper() == Looper.myLooper()) {
            block(activity!!)
            null
        } else {
            // Launch a Job on the UI context and check again if the activity and view are still valid
            GlobalScope.launch(Dispatchers.Main) {
                if (activity != null && view != null && context != null) {
                    block(activity!!)
                }
            }
        }
    }

    return null
}