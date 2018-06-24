package chat.rocket.android.util

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

object DateTimeHelper {

    /**
     * Returns a [LocalDateTime] from a [Long].
     *
     * @param long The [Long] to gets a [LocalDateTime].
     * @return The [LocalDateTime] from a [Long].
     */
    fun getLocalDateTime(long: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(long), ZoneId.systemDefault())
    }

    /**
     * Returns a time from a [LocalDateTime].
     *
     * @param localDateTime The [LocalDateTime].
     * @return The time from a [LocalDateTime].
     */
    fun getTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        return localDateTime.toLocalTime().format(formatter).toString()
    }
}