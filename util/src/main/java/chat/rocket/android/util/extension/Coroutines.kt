package chat.rocket.android.util.extension

import chat.rocket.android.core.lifecycle.CancelStrategy
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

/**
 * Launches a coroutine on the UI context.
 *
 * @param strategy a CancelStrategy for canceling the coroutine job
 */
fun launchUI(strategy: CancelStrategy, block: suspend CoroutineScope.() -> Unit): Job {
    return launch(context = UI, parent = strategy.jobs, block = block)
}