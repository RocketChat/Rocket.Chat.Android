package chat.rocket.android.util

import chat.rocket.common.RocketChatNetworkErrorException
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

const val DEFAULT_RETRY = 3

suspend fun <T> retryIO(
        description: String = "<missing description>",
        times: Int = DEFAULT_RETRY,
        initialDelay: Long = 100, // 0.1 second
        maxDelay: Long = 1000,    // 1 second
        factor: Double = 2.0,
        block: suspend () -> T): T
{
    var currentDelay = initialDelay
    repeat(times - 1) { currentTry ->
        try {
            return block()
        } catch (e: RocketChatNetworkErrorException) {
            Timber.d(e, "failed call($currentTry): $description")
            e.printStackTrace()
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}