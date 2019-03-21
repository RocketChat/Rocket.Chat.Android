package chat.rocket.android.extensions

import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@JvmName("awaitVoid")
suspend fun Task<Void>.await() = suspendCoroutine<Unit> { continuation ->
    addOnSuccessListener { continuation.resume(Unit) }
    addOnFailureListener { continuation.resumeWithException(it) }
}

suspend fun <TResult> Task<TResult>.await() = suspendCoroutine<TResult> { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}