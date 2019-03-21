package chat.rocket.android.util.extension

import chat.rocket.android.core.lifecycle.CancelStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Launches a coroutine on the UI context.
 *
 * @param strategy a CancelStrategy for canceling the coroutine job
 */
fun launchUI(strategy: CancelStrategy, block: suspend CoroutineScope.() -> Unit): Job =
    MainScope().launch(context = strategy.jobs, block = block)
