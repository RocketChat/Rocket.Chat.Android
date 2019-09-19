package chat.rocket.android.util.extensions

import org.threeten.bp.LocalDateTime

fun Long?.localDateTime(): LocalDateTime? {
    return this?.let {
        DateTimeHelper.getLocalDateTime(it)
    }
}

fun Long?.isGreaterThan(value: Int): Boolean {
    this?.let {
        return it > value
    }
    return false
}