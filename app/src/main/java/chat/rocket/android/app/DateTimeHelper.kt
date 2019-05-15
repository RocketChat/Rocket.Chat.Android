import android.content.Context
import chat.rocket.android.R
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Period
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.format.TextStyle
import java.util.*

object DateTimeHelper {
    private val today = LocalDate.now()
    private val yesterday = today.minusDays(1)
    private val lastWeek = today.minusWeeks(1)

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
     * Returns a date from a [LocalDateTime] or the textual representation if the [LocalDateTime]
     * has a max period of a week from the current date.
     *
     * @param localDateTime The [LocalDateTime].
     * @param context The context.
     * @return The date or the textual representation from a [LocalDateTime].
     */
    fun getDate(localDateTime: LocalDateTime, context: Context): String {
        val localDate = localDateTime.toLocalDate()
        return when (localDate) {
            today -> formatLocalTime(localDateTime.toLocalTime())
            yesterday -> context.getString(R.string.msg_yesterday)
            else -> {
                if (Period.between(lastWeek, localDate).days <= 0) {
                    formatLocalDate(localDate)
                } else {
                    localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                }
            }
        }
    }

    fun getFormattedDateForMessages(localDateTime: LocalDateTime, context: Context): String {
        val localDate = localDateTime.toLocalDate()
        return when (localDate) {
            today -> context.getString(R.string.msg_today)
            yesterday -> context.getString(R.string.msg_yesterday)
            else -> formatLocalDate(localDate)
        }
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

    /**
     * Returns a date time from a [LocalDateTime].
     *
     * @param localDateTime The [LocalDateTime].
     * @return The time from a [LocalDateTime].
     */
    fun getDateTime(localDateTime: LocalDateTime): String {
        return formatLocalDateTime(localDateTime)
    }

    private fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        return localDateTime.format(formatter).toString()
    }

    private fun formatLocalDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        return localDate.format(formatter).toString()
    }

    private fun formatLocalTime(localTime: LocalTime): String {
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        return localTime.format(formatter).toString()
    }
}