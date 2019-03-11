package chat.rocket.android.util

import android.database.sqlite.SQLiteDatabaseLockedException
import chat.rocket.common.RocketChatNetworkErrorException
import kotlinx.coroutines.experimental.TimeoutCancellationException
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.isActive
import timber.log.Timber
import kotlin.coroutines.experimental.coroutineContext

const val DEFAULT_RETRY = 3
private const val DEFAULT_DB_RETRY = 15

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
        if (!coroutineContext.isActive) throw TimeoutCancellationException("job canceled")
        try {
            return block()
        } catch (e: RocketChatNetworkErrorException) {
            Timber.d(e, "failed call($currentTry): $description")
            e.printStackTrace()
        }

        if (!coroutineContext.isActive) throw TimeoutCancellationException("job canceled")
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }

    if (!coroutineContext.isActive) throw TimeoutCancellationException("job canceled")
    return block() // last attempt
}

suspend fun <T> retryDB(
        description: String = "<missing description>",
        times: Int = DEFAULT_DB_RETRY,
        initialDelay: Long = 100, // 0.1 second
        maxDelay: Long = 1500,    // 1.5 second
        factor: Double = 1.2,
        block: suspend () -> T): T
{
    var currentDelay = initialDelay
    repeat(times - 1) { currentTry ->
        if (!coroutineContext.isActive) throw TimeoutCancellationException("job canceled")
        try {
            return block()
        } catch (e: SQLiteDatabaseLockedException) {
            Timber.d(e, "failed call($currentTry): $description")
            e.printStackTrace()
        }

        if (!coroutineContext.isActive) throw TimeoutCancellationException("job canceled")
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }

    if (!coroutineContext.isActive) throw TimeoutCancellationException("job canceled")
    return block() // last attempt
}